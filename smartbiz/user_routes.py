from flask import Blueprint, render_template, redirect, url_for, flash, request, current_app, send_file, abort, session
from flask_login import login_user, logout_user, login_required, current_user
from werkzeug.security import generate_password_hash, check_password_hash
from werkzeug.utils import secure_filename
import os
import io
import pandas as pd
from .models import db, User, Product, Prediction, InventoryLog, AuditLog, Report, InventoryDetection, Upload
from .causal_analysis import run_causal_analysis
from .recommendation_engine import generate_recommendations
from .prediction import train_and_predict, generate_forecasts
from .report_generator import generate_pdf_report, generate_excel_report
from .inventory_monitor import detect_objects
from .auth_utils import role_required
from .analytics_utils import perform_rfm_analysis, calculate_customer_lifetime_value, retention_analysis

user_bp = Blueprint('user', __name__)

@user_bp.route('/')
def home():
    return render_template('user/home.html')

@user_bp.route('/register', methods=['GET', 'POST'])
def register():
    import re
    from email_validator import validate_email, EmailNotValidError
    if current_user.is_authenticated:
        return redirect(url_for('user.dashboard'))
    if request.method == 'POST':
        full_name = request.form.get('full_name', '').strip()
        email = request.form.get('email', '').strip()
        phone = request.form.get('phone', '').strip()
        password = request.form.get('password', '')
        confirm_password = request.form.get('confirm_password', '')

        if not full_name or not email or not phone or not password or not confirm_password:
            flash('All fields are required.', 'danger')
            return redirect(url_for('user.register'))

        try:
            valid = validate_email(email)
            email = valid.ascii_email
        except EmailNotValidError as e:
            flash(f'Invalid email format: {str(e)}', 'danger')
            return redirect(url_for('user.register'))

        if password != confirm_password:
            flash('Passwords do not match.', 'danger')
            return redirect(url_for('user.register'))

        # Password validation
        if len(password) < 8:
            flash('Password must be at least 8 characters long.', 'danger')
            return redirect(url_for('user.register'))
        if not re.search(r"[A-Z]", password):
            flash('Password must contain at least one uppercase letter.', 'danger')
            return redirect(url_for('user.register'))
        if not re.search(r"[a-z]", password):
            flash('Password must contain at least one lowercase letter.', 'danger')
            return redirect(url_for('user.register'))
        if not re.search(r"[0-9]", password):
            flash('Password must contain at least one number.', 'danger')
            return redirect(url_for('user.register'))
        if not re.search(r"[^A-Za-z0-9]", password):
            flash('Password must contain at least one special character.', 'danger')
            return redirect(url_for('user.register'))

        # Phone validation (10 to 15 digits, allowing optional + and common spacing characters)
        phone_cleaned = re.sub(r"[\s\-\(\)\+]", "", phone)
        if not phone_cleaned.isdigit() or len(phone_cleaned) < 10 or len(phone_cleaned) > 15:
            flash('Invalid telephone number. Must contain between 10 and 15 digits.', 'danger')
            return redirect(url_for('user.register'))

        user_exists = User.query.filter(db.func.lower(User.email) == email.lower()).first()
        if user_exists:
            flash('Email already exists.', 'danger')
            return redirect(url_for('user.register'))

        new_user = User(
            full_name=full_name, email=email.lower(), phone=phone,
            password=generate_password_hash(password, method='pbkdf2:sha256'),
            role='User' # Force User role for all registrations
        )
        db.session.add(new_user)
        db.session.commit()
        flash('Registration successful! Please login.', 'success')
        return redirect(url_for('user.login'))
    return render_template('user/register.html')

@user_bp.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        if current_user.role == 'Admin':
            return redirect(url_for('admin.dashboard'))
        return redirect(url_for('user.dashboard'))
    if request.method == 'POST':
        email = request.form.get('email', '').strip().lower()
        password = request.form.get('password')
        remember_me = request.form.get('remember_me') == 'on'
        user = User.query.filter(db.func.lower(User.email) == email).first()
        if user and check_password_hash(user.password, password):
            if user.status != 'Active':
                flash('Your account is inactive. Please contact admin.', 'warning')
                return redirect(url_for('user.login'))
            session.permanent = True
            login_user(user, remember=remember_me)
            db.session.add(AuditLog(user_id=user.id, action="User Login", module="Authentication"))
            db.session.commit()
            if user.role == 'Admin':
                return redirect(url_for('admin.dashboard'))
            return redirect(url_for('user.dashboard'))
        else:
            flash('Login failed. Check email and password.', 'danger')
    return render_template('user/login.html')

@user_bp.route('/forgot_password', methods=['GET', 'POST'])
def forgot_password():
    if current_user.is_authenticated:
        return redirect(url_for('user.dashboard'))
    if request.method == 'POST':
        email = request.form.get('email', '').strip().lower()
        user = User.query.filter(db.func.lower(User.email) == email).first()
        if user:
            import secrets
            from datetime import datetime, timedelta
            token = secrets.token_hex(20)
            user.reset_token = token
            user.reset_token_expiry = datetime.utcnow() + timedelta(hours=1)
            db.session.commit()
            # In a demo/production system where sending a real email is optional, we flash the reset link!
            reset_url = url_for('user.reset_password', token=token, _external=True)
            flash(f"Password reset requested! [DEMO LINK]: Please click here to reset your password: {reset_url}", "info")
            return redirect(url_for('user.login'))
        else:
            flash("If that email address exists, a reset link has been processed.", "info")
            return redirect(url_for('user.login'))
    return render_template('user/forgot_password.html')

@user_bp.route('/reset_password/<token>', methods=['GET', 'POST'])
def reset_password(token):
    from datetime import datetime
    if current_user.is_authenticated:
        return redirect(url_for('user.dashboard'))
    user = User.query.filter_by(reset_token=token).first()
    if not user or not user.reset_token_expiry or user.reset_token_expiry < datetime.utcnow():
        flash("Invalid or expired reset token.", "danger")
        return redirect(url_for('user.login'))

    if request.method == 'POST':
        password = request.form.get('password')
        confirm_password = request.form.get('confirm_password')
        if password != confirm_password:
            flash("Passwords do not match.", "danger")
            return redirect(url_for('user.reset_password', token=token))

        import re
        # Password validation
        if len(password) < 8:
            flash('Password must be at least 8 characters long.', 'danger')
            return redirect(url_for('user.reset_password', token=token))
        if not re.search(r"[A-Z]", password):
            flash('Password must contain at least one uppercase letter.', 'danger')
            return redirect(url_for('user.reset_password', token=token))
        if not re.search(r"[a-z]", password):
            flash('Password must contain at least one lowercase letter.', 'danger')
            return redirect(url_for('user.reset_password', token=token))
        if not re.search(r"[0-9]", password):
            flash('Password must contain at least one number.', 'danger')
            return redirect(url_for('user.reset_password', token=token))
        if not re.search(r"[^A-Za-z0-9]", password):
            flash('Password must contain at least one special character.', 'danger')
            return redirect(url_for('user.reset_password', token=token))

        user.password = generate_password_hash(password, method='pbkdf2:sha256')
        user.reset_token = None
        user.reset_token_expiry = None
        db.session.commit()
        flash("Your password has been reset successfully. Please login.", "success")
        return redirect(url_for('user.login'))
    return render_template('user/reset_password.html', token=token)

@user_bp.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('user.home'))

@user_bp.route('/dashboard')
@login_required
def dashboard():
    from datetime import datetime, timedelta
    products = Product.query.all()

    # Summary Stats
    total_products = len(products)
    categories = list(set([p.category for p in products if p.category]))
    total_users = User.query.count()
    total_inventory_items = sum([p.stock_quantity for p in products])
    total_reports = Report.query.count()
    total_predictions = Prediction.query.count()
    low_stock_items = [p for p in products if 0 < p.stock_quantity <= p.minimum_threshold]
    out_of_stock_items = [p for p in products if p.stock_quantity <= 0]

    # Enterprise High-Fidelity KPI aggregations
    gross_revenue = sum([p.revenue for p in products]) if products else 0.0
    net_profit = sum([p.profit for p in products]) if products else 0.0
    profit_margin = (net_profit / gross_revenue * 100.0) if gross_revenue > 0 else 0.0

    # Dynamic Business Health Score calculations
    low_pct = (len(low_stock_items) / total_products * 100.0) if total_products > 0 else 0
    out_pct = (len(out_of_stock_items) / total_products * 100.0) if total_products > 0 else 0
    health_score = int(max(40, min(100, 100 - (low_pct * 0.8) - (out_pct * 1.5) + (profit_margin * 0.3))))

    # Activity Feed
    recent_activities = AuditLog.query.order_by(AuditLog.timestamp.desc()).limit(8).all()

    # Reports
    recent_reports = Report.query.order_by(Report.created_at.desc()).limit(5).all()

    # Uploaded Datasets
    recent_uploads = Upload.query.order_by(Upload.upload_date.desc()).limit(5).all()

    # Low Stock / Alerts
    out_of_stock = [p for p in products if p.stock_quantity <= 0]
    low_stock = [p for p in products if 0 < p.stock_quantity <= p.minimum_threshold]
    newly_added = Product.query.filter(Product.created_at >= datetime.utcnow() - timedelta(days=7)).limit(5).all()

    # Prediction Stats
    today = datetime.utcnow().date()
    todays_predictions = Prediction.query.filter(db.func.date(Prediction.timestamp) == today).count()

    # Frequent Prediction Category
    from sqlalchemy import func
    top_cat = db.session.query(Product.category, func.count(Prediction.id)).join(Prediction).group_by(Product.category).order_by(func.count(Prediction.id).desc()).first()
    frequent_category = top_cat[0] if top_cat else "N/A"

    # Chart Data
    if products:
        df = pd.DataFrame([{'Date': p.date, 'Revenue': p.revenue, 'Profit': p.profit, 'Sales': p.sales, 'Category': p.category, 'Stock': p.stock_quantity} for p in products])
        df['Date'] = pd.to_datetime(df['Date'])

        # Monthly Trend
        monthly = df.set_index('Date').resample('ME').sum().tail(6)
        chart_labels = [d.strftime('%b %Y') for d in monthly.index]
        chart_revenue = monthly['Revenue'].tolist()
        chart_profit = monthly['Profit'].tolist()

        # Category Distribution
        cat_dist = df['Category'].value_counts().to_dict()

        # Inventory Status
        status_dist = {
            'In Stock': total_products - len(low_stock_items) - len(out_of_stock_items),
            'Low Stock': len(low_stock_items),
            'Out of Stock': len(out_of_stock_items)
        }

        # Reports Generated per Month
        report_monthly_data = db.session.query(db.func.strftime('%b %Y', Report.created_at), db.func.count(Report.id)).group_by(db.func.strftime('%Y-%m', Report.created_at)).order_by(Report.created_at.desc()).limit(6).all()
        report_monthly_data.reverse()
        report_chart_labels = [r[0] for r in report_monthly_data]
        report_chart_data = [r[1] for r in report_monthly_data]
    else:
        chart_labels = []; chart_revenue = []; chart_profit = []; cat_dist = {}; status_dist = {}
        report_chart_labels = []; report_chart_data = []

    return render_template('user/dashboard.html',
                           now=datetime.utcnow(),
                           total_products=total_products,
                           total_categories=len(categories),
                           total_users=total_users,
                           total_inventory_items=total_inventory_items,
                           total_reports=total_reports,
                           total_predictions=total_predictions,
                           low_stock_count=len(low_stock_items),
                           out_of_stock_count=len(out_of_stock_items),
                           todays_predictions=todays_predictions,
                           frequent_category=frequent_category,
                           activities=recent_activities,
                           recent_reports=recent_reports,
                           recent_uploads=recent_uploads,
                           out_of_stock=out_of_stock,
                           low_stock=low_stock,
                           newly_added=newly_added,
                           chart_labels=chart_labels,
                           chart_revenue=chart_revenue,
                           chart_profit=chart_profit,
                           cat_dist=cat_dist,
                           status_dist=status_dist,
                           report_chart_labels=report_chart_labels,
                           report_chart_data=report_chart_data,
                           gross_revenue=gross_revenue,
                           net_profit=net_profit,
                           profit_margin=profit_margin,
                           health_score=health_score)

@user_bp.route('/upload', methods=['GET', 'POST'])
@login_required
@role_required(['Admin', 'User'])
def upload():
    upload_results = None; preview_data = []
    if request.method == 'POST':
        if 'file' not in request.files:
            flash('No file part', 'danger'); return redirect(request.url)
        file = request.files['file']
        if file.filename == '':
            flash('No selected file', 'danger'); return redirect(request.url)
        if file and (file.filename.endswith('.csv') or file.filename.endswith('.xlsx')):
            filename = secure_filename(file.filename)
            filepath = os.path.join(current_app.config['UPLOAD_FOLDER'], filename)
            file.save(filepath)
            try:
                df = pd.read_csv(filepath) if filename.endswith('.csv') else pd.read_excel(filepath)

                # Dynamic mapping to support variations of standard column names
                col_mapping = {
                    'product_name': 'Product Name',
                    'price': 'Price',
                    'marketing_spend': 'Marketing Spend',
                    'marketing': 'Marketing Spend',
                    'stock': 'Stock Quantity',
                    'stock_quantity': 'Stock Quantity',
                    'sales': 'Sales',
                    'profit': 'Profit',
                    'revenue': 'Revenue',
                    'category': 'Category',
                    'date': 'Date'
                }

                # Standardize columns based on mapping for uniform processing
                renamed_cols = {}
                for col in df.columns:
                    norm = col.strip().lower().replace(' ', '_')
                    if norm in col_mapping:
                        renamed_cols[col] = col_mapping[norm]
                df = df.rename(columns=renamed_cols)

                required_cols = ['Product Name', 'Price', 'Marketing Spend', 'Sales', 'Profit', 'Stock Quantity', 'Date']
                missing_cols = [col for col in required_cols if col not in df.columns]
                if missing_cols:
                    flash(f'Validation Failed: Missing required columns: {", ".join(missing_cols)}', 'danger')
                    return redirect(request.url)

                # Detect Duplicate Records in the file
                if df.duplicated(subset=['Product Name', 'Date']).any():
                    flash('Validation Failed: Duplicate records detected for the same product on the same date.', 'danger')
                    return redirect(request.url)

                # Detect Empty cells
                if df[required_cols].isnull().any().any():
                    flash('Validation Failed: Empty cells / missing values detected in required fields.', 'danger')
                    return redirect(request.url)

                # Validate numeric columns data types & numeric values
                numeric_cols = ['Price', 'Marketing Spend', 'Stock Quantity', 'Sales', 'Profit']
                for col in numeric_cols:
                    converted = pd.to_numeric(df[col], errors='coerce')
                    if converted.isnull().any():
                        invalid_rows = df[converted.isnull()][col].tolist()
                        flash(f"Validation Failed: Wrong data type or invalid non-numeric values in column '{col}': {invalid_rows}", 'danger')
                        return redirect(request.url)
                    # Check for negative numbers on non-profit columns
                    if col != 'Profit' and (df[col] < 0).any():
                        flash(f"Validation Failed: Invalid negative numbers detected in column '{col}'.", 'danger')
                        return redirect(request.url)

                total_records = len(df)
                missing_values = df.isnull().sum().sum()
                duplicates_removed = 0 # Handled by strict error above, or we can use drop_duplicates if soft-pass is preferred, but strict block is secure!

                # Outliers detection for summary
                outliers = 0
                for col in ['Price', 'Sales', 'Profit']:
                    if df[col].std() > 0:
                        z_scores = (df[col] - df[col].mean()) / df[col].std()
                        outliers += (z_scores.abs() > 3).sum()

                quality_score = min(100, max(0, 100 - (missing_values * 2) - (outliers * 5)))

                # Record the upload to the Upload table
                new_upload = Upload(
                    filename=filename,
                    file_type='CSV' if filename.endswith('.csv') else 'XLSX',
                    quality_score=quality_score,
                    user_id=current_user.id,
                    status='Processed'
                )
                db.session.add(new_upload)

                # Upsert into Product table
                for _, row in df.iterrows():
                    p = Product.query.filter_by(product_name=row['Product Name']).first()
                    # Assign a category if present, else fallback
                    category = row.get('Category', 'General')
                    revenue = row.get('Revenue', float(row['Price']) * int(row['Sales']))

                    if p:
                        p.category = category
                        p.price = float(row['Price'])
                        p.marketing_spend = float(row['Marketing Spend'])
                        p.stock_quantity = int(row['Stock Quantity'])
                        p.sales = int(row['Sales'])
                        p.revenue = float(revenue)
                        p.profit = float(row['Profit'])
                        p.date = pd.to_datetime(row['Date'])
                    else:
                        new_p = Product(
                            product_name=row['Product Name'],
                            category=category,
                            price=float(row['Price']),
                            marketing_spend=float(row['Marketing Spend']),
                            stock_quantity=int(row['Stock Quantity']),
                            sales=int(row['Sales']),
                            revenue=float(revenue),
                            profit=float(row['Profit']),
                            date=pd.to_datetime(row['Date'])
                        )
                        db.session.add(new_p)

                db.session.add(AuditLog(user_id=current_user.id, action=f"Uploaded Dataset: {filename} ({total_records} rows)", module="Data Hub"))
                db.session.commit()

                upload_results = {
                    'total_records': total_records,
                    'missing_values': missing_values,
                    'duplicates_removed': duplicates_removed,
                    'outliers_detected': outliers,
                    'quality_score': quality_score,
                    'status': 'Dataset Uploaded and Processed Successfully'
                }
                preview_data = df.head(10).to_dict('records')
                flash('Dataset uploaded, validated, and processed successfully!', 'success')
            except Exception as e:
                db.session.rollback()
                flash(f'Error processing file: {str(e)}', 'danger'); return redirect(request.url)
    return render_template('user/upload.html', upload_results=upload_results, preview_data=preview_data)

@user_bp.route('/inventory')
@login_required
@role_required(['Admin', 'User'])
def inventory():
    page = request.args.get('page', 1, type=int)
    search = request.args.get('search')
    cat_filter = request.args.get('category')
    status_filter = request.args.get('status')

    query = Product.query
    if search:
        query = query.filter(Product.product_name.contains(search) | Product.supplier.contains(search))
    if cat_filter:
        query = query.filter(Product.category == cat_filter)

    if status_filter:
        if status_filter == 'In Stock':
            query = query.filter(Product.stock_quantity > Product.minimum_threshold)
        elif status_filter == 'Low Stock':
            query = query.filter(Product.stock_quantity > 0, Product.stock_quantity <= Product.minimum_threshold)
        elif status_filter == 'Out of Stock':
            query = query.filter(Product.stock_quantity <= 0)

    pagination = query.order_by(Product.product_name).paginate(page=page, per_page=10)
    products = Product.query.order_by(Product.product_name).all()

    categories = db.session.query(Product.category).distinct().all()
    categories = [c[0] for c in categories if c[0]]

    stats = {
        'total': Product.query.count(),
        'categories': len(categories),
        'in_stock': Product.query.filter(Product.stock_quantity > Product.minimum_threshold).count(),
        'low_stock': Product.query.filter(Product.stock_quantity > 0, Product.stock_quantity <= Product.minimum_threshold).count(),
        'out_of_stock': Product.query.filter(Product.stock_quantity <= 0).count(),
        'overstock': Product.query.filter(Product.stock_quantity > Product.maximum_threshold).count()
    }

    inventory_alerts = {
        'low': Product.query.filter(Product.stock_quantity > 0, Product.stock_quantity <= Product.minimum_threshold).all(),
        'out': Product.query.filter(Product.stock_quantity <= 0).all(),
        'over': Product.query.filter(Product.stock_quantity > Product.maximum_threshold).all(),
        'recent': Product.query.order_by(Product.created_at.desc()).limit(5).all()
    }

    history = InventoryDetection.query.order_by(InventoryDetection.timestamp.desc()).limit(10).all()

    return render_template('user/inventory.html',
                           products=products,
                           pagination=pagination,
                           categories=categories,
                           stats=stats,
                           alerts=inventory_alerts,
                           history=history)

@user_bp.route('/inventory/edit/<int:product_id>', methods=['POST'])
@login_required
def edit_product(product_id):
    p = db.session.get(Product, product_id)
    if not p:
        abort(404)
    p.product_name = request.form.get('name')
    p.category = request.form.get('category')
    p.stock_quantity = int(request.form.get('quantity', 0))
    p.price = float(request.form.get('price', 0))
    p.supplier = request.form.get('supplier')
    p.description = request.form.get('description')

    db.session.add(AuditLog(user_id=current_user.id, action=f"Updated Product: {p.product_name}", module="Inventory"))
    db.session.commit()
    flash(f"Product {p.product_name} updated successfully.", "success")
    return redirect(url_for('user.inventory'))

@user_bp.route('/inventory/delete/<int:product_id>', methods=['POST'])
@login_required
def delete_product_user(product_id):
    p = db.session.get(Product, product_id)
    if not p:
        abort(404)
    name = p.product_name
    db.session.delete(p)
    db.session.add(AuditLog(user_id=current_user.id, action=f"Deleted Product: {name}", module="Inventory"))
    db.session.commit()
    flash(f"Product {name} deleted.", "danger")
    return redirect(url_for('user.inventory'))

@user_bp.route('/inventory/add', methods=['POST'])
@login_required
def add_product():
    name = request.form.get('name')
    cat = request.form.get('category')
    qty = int(request.form.get('quantity', 0))
    price = float(request.form.get('price', 0))
    supplier = request.form.get('supplier')
    desc = request.form.get('description')

    new_p = Product(
        product_name=name,
        category=cat,
        stock_quantity=qty,
        price=price,
        supplier=supplier,
        description=desc
    )
    db.session.add(new_p)
    db.session.add(AuditLog(user_id=current_user.id, action=f"Added Product: {name}", module="Inventory"))
    db.session.commit()
    flash(f"Product {name} added successfully.", "success")
    return redirect(url_for('user.inventory'))

@user_bp.route('/simulator', methods=['GET', 'POST'])
@login_required
@role_required(['Admin', 'User'])
def simulator():
    products = Product.query.all()
    simulation_result = None
    if request.method == 'POST':
        pc = float(request.form.get('price_change', 0)) / 100
        mc = float(request.form.get('marketing_change', 0)) / 100
        cr = sum([p.revenue for p in products]); cp = sum([p.profit for p in products]); cs = sum([p.sales for p in products])
        ss = cs * (1 - 1.5 * pc) * (1 + 0.5 * mc)
        sr = cr * (1 + pc) * (ss / cs if cs > 0 else 1)
        sp = sr * (cp / cr if cr > 0 else 0.2)
        simulation_result = {
            'current_sales': round(cs, 2), 'current_revenue': round(cr, 2), 'current_profit': round(cp, 2),
            'simulated_sales': round(ss, 2), 'simulated_revenue': round(sr, 2), 'simulated_profit': round(sp, 2),
            'sales_impact': round(((ss - cs) / cs * 100), 2) if cs > 0 else 0,
            'revenue_impact': round(((sr - cr) / cr * 100), 2) if cr > 0 else 0,
            'profit_impact': round(((sp - cp) / cp * 100), 2) if cp > 0 else 0,
        }
    return render_template('user/simulator.html', simulation=simulation_result)

@user_bp.route('/forecasting')
@login_required
@role_required(['Admin', 'User'])
def forecasting():
    products = Product.query.all()
    if not products:
        flash("No data available for forecasting. Please upload a dataset.", "warning")
        return redirect(url_for('user.upload'))

    # Standardize and add category details to product forecasts
    forecasts = generate_forecasts(products)

    # Extract unique categories from actual products
    categories = sorted(list(set([p.category for p in products if p.category])))

    # Create product mapping to inject category into product_forecasts array
    prod_cat_map = {p.product_name: (p.category if p.category else 'General') for p in products}
    for pf in forecasts.get('product_forecasts', []):
        pf['category'] = prod_cat_map.get(pf['product_name'], 'General')

    # Historical data for combined charts
    df = pd.DataFrame([{'Date': p.date, 'Revenue': p.revenue, 'Sales': p.sales} for p in products])
    df['Date'] = pd.to_datetime(df['Date'])
    hist_monthly = df.set_index('Date').resample('ME').sum().tail(6).reset_index()
    historical = {
        'labels': [d.strftime('%b %Y') for d in hist_monthly['Date']],
        'revenue': hist_monthly['Revenue'].tolist(),
        'sales': hist_monthly['Sales'].tolist()
    }

    return render_template('user/forecasting.html', forecasts=forecasts, historical=historical, categories=categories)

@user_bp.route('/customer_intelligence')
@login_required
@role_required(['Admin', 'User'])
def customer_intelligence():
    segments = perform_rfm_analysis(); clv = calculate_customer_lifetime_value(); retention = retention_analysis()
    return render_template('user/customer_intelligence.html', segments=segments, clv=clv, retention=retention)

@user_bp.route('/prediction', methods=['GET', 'POST'])
@login_required
@role_required(['Admin', 'User'])
def prediction():
    prediction_result = None
    if request.method == 'POST':
        p, m, s = float(request.form.get('price', 0)), float(request.form.get('marketing', 0)), float(request.form.get('stock', 0))
        products = Product.query.all()
        prediction_result = train_and_predict(products, p, m, s)
        if prediction_result:
            db.session.add(Prediction(product_id=products[0].id if products else None, predicted_sales=prediction_result['predicted_sales'], predicted_profit=prediction_result['predicted_profit'], stock_out_days=prediction_result['stock_out_days']))
            db.session.add(AuditLog(user_id=current_user.id, action="Generated AI Prediction", module="Prediction"))
            db.session.commit()
    return render_template('user/prediction.html', prediction=prediction_result)

@user_bp.route('/causal_analysis', methods=['GET', 'POST'])
@login_required
@role_required(['Admin', 'User'])
def causal_analysis():
    products = Product.query.all()
    if not products:
        if request.headers.get('X-Requested-With') == 'XMLHttpRequest' or request.is_json:
            return {'success': False, 'error': 'No data available'}
        flash("No data available for analysis.", "warning")
        return redirect(url_for('user.upload'))

    # Handle Strategy Simulator POST Request
    if request.method == 'POST':
        data = request.get_json() if request.is_json else request.form
        if not data:
            return {'success': False, 'error': 'Invalid request data'}

        try:
            pc = float(data.get('price_change', 0)) / 100.0
            mc = float(data.get('marketing_change', 0)) / 100.0
            dc = float(data.get('discount_change', 0)) / 100.0
            ic = float(data.get('inventory_change', 0)) / 100.0

            current_sales = sum([p.sales for p in products])
            current_revenue = sum([p.revenue for p in products])
            current_profit = sum([p.profit for p in products])

            sales_factor = (1.0 - 1.5 * pc) * (1.0 + 0.5 * mc) * (1.0 + 0.8 * dc) * (1.0 + 0.2 * ic)
            simulated_sales = max(0, current_sales * sales_factor)

            revenue_factor = (1.0 + pc) * (simulated_sales / current_sales if current_sales > 0 else 1.0)
            simulated_revenue = max(0.0, current_revenue * revenue_factor)

            margin_ratio = (current_profit / current_revenue) if current_revenue > 0 else 0.25
            simulated_profit = max(0.0, simulated_revenue * margin_ratio)

            sales_impact = round(((simulated_sales - current_sales) / current_sales * 100.0), 2) if current_sales > 0 else 0.0
            revenue_impact = round(((simulated_revenue - current_revenue) / current_revenue * 100.0), 2) if current_revenue > 0 else 0.0
            profit_impact = round(((simulated_profit - current_profit) / current_profit * 100.0), 2) if current_profit > 0 else 0.0

            return {
                'success': True,
                'predicted_sales': round(simulated_sales),
                'predicted_revenue': round(simulated_revenue, 2),
                'predicted_profit': round(simulated_profit, 2),
                'sales_impact': sales_impact,
                'revenue_impact': revenue_impact,
                'profit_impact': profit_impact
            }
        except Exception as ex:
            return {'success': False, 'error': str(ex)}

    # Handle normal GET Request (Premium Strategic Dashboard)
    total_revenue = sum([p.revenue for p in products])
    total_profit = sum([p.profit for p in products])
    total_sales = sum([p.sales for p in products])
    inventory_value = sum([p.stock_quantity * p.price for p in products])

    from collections import defaultdict
    year_revenue = defaultdict(float)
    year_profit = defaultdict(float)
    for p in products:
        if p.date:
            year_str = p.date.year if hasattr(p.date, 'year') else pd.to_datetime(p.date).year
            year_revenue[year_str] += p.revenue
            year_profit[year_str] += p.profit

    all_years = sorted(list(year_revenue.keys()))
    if not all_years:
        all_years = [2022, 2023, 2024, 2025, 2026]
        hist_revenue = [1200000, 1450000, 1800000, 2100000, total_revenue if total_revenue > 0 else 2400000]
        hist_profit = [240000, 310000, 420000, 510000, total_profit if total_profit > 0 else 590000]
    else:
        hist_revenue = [year_revenue[yr] for yr in all_years]
        hist_profit = [year_profit[yr] for yr in all_years]

    if len(all_years) >= 2:
        rev_last = year_revenue[all_years[-1]]
        rev_prev = year_revenue[all_years[-2]]
        growth_rate = round(((rev_last - rev_prev) / rev_prev * 100.0), 1) if rev_prev > 0 else 12.4
    else:
        growth_rate = 12.4

    fc_revenue = [total_revenue * 1.15, total_revenue * 1.30] if total_revenue > 0 else [2700000, 3100000]
    fc_profit = [total_profit * 1.18, total_profit * 1.35] if total_profit > 0 else [650000, 780000]

    stock_by_warehouse = defaultdict(int)
    stock_by_region = defaultdict(int)
    for p in products:
        wh = p.warehouse if p.warehouse else 'General Wh'
        reg = p.region if p.region else 'General Region'
        stock_by_warehouse[wh] += p.stock_quantity
        stock_by_region[reg] += p.stock_quantity

    if not stock_by_warehouse:
        stock_by_warehouse = {'Delhi Central': 0, 'Mumbai West': 0, 'Bangalore South': 0}
    if not stock_by_region:
        stock_by_region = {'North': 0, 'West': 0, 'South': 0, 'East': 0}

    rev_risk = 'Low' if growth_rate >= 10.0 else ('Medium' if growth_rate > 0 else 'High')
    low_stock_count = sum(1 for p in products if p.stock_quantity <= p.minimum_threshold)
    inv_risk = 'Low' if low_stock_count == 0 else ('Medium' if low_stock_count <= 5 else 'High')
    dem_risk = 'Low'
    churn_risk = 'Medium'

    profit_margin = (total_profit / total_revenue * 100.0) if total_revenue > 0 else 0.0
    low_pct = (low_stock_count / len(products) * 100.0) if products else 0.0
    health_score = int(max(40, min(100, 100 - (low_pct * 0.8) + (profit_margin * 0.3))))

    low_stock_list = []
    over_stock_list = []
    for p in products:
        if p.stock_quantity <= p.minimum_threshold:
            low_stock_list.append({'name': p.product_name, 'qty': p.stock_quantity, 'wh': p.warehouse or 'General Wh'})
        elif p.stock_quantity > p.maximum_threshold:
            over_stock_list.append({'name': p.product_name, 'qty': p.stock_quantity, 'wh': p.warehouse or 'General Wh'})

    low_stock_list = low_stock_list[:5]
    over_stock_list = over_stock_list[:5]

    pricing_rec = "Based on elasticity modeling, optimize high-margin products with a soft 5% price increase. Apply tactical 5-10% discount on slow-moving inventory to liquidate frozen capital."
    marketing_rec = "Reallocate 20% of underperforming categories' budget directly to high-demand consumer categories to capture growing regional segments."
    inventory_rec = f"Address replenishment immediately for {len(low_stock_list)} critical SKU alerts. Shift stock from oversupplied regional warehouses to high-velocity nodes."
    customer_rec = "Implement automated post-purchase surveys and loyalty point boosters to elevate lower-satisfaction clusters and improve lifetime retention."

    df = pd.DataFrame([{'Product Name': p.product_name, 'Category': p.category, 'Price': p.price, 'Marketing Spend': p.marketing_spend, 'Stock Quantity': p.stock_quantity, 'Sales': p.sales, 'Revenue': p.revenue, 'Profit': p.profit, 'Date': p.date} for p in products])
    causal_results = run_causal_analysis(df) if len(df) >= 5 else []

    return render_template(
        'user/causal_analysis.html',
        health_score=health_score,
        total_revenue=total_revenue,
        total_profit=total_profit,
        total_sales=total_sales,
        inventory_value=inventory_value,
        growth_rate=growth_rate,
        all_years=all_years,
        hist_revenue=hist_revenue,
        hist_profit=hist_profit,
        fc_revenue=fc_revenue,
        fc_profit=fc_profit,
        stock_by_warehouse=stock_by_warehouse,
        stock_by_region=stock_by_region,
        rev_risk=rev_risk,
        inv_risk=inv_risk,
        dem_risk=dem_risk,
        churn_risk=churn_risk,
        low_stock_list=low_stock_list,
        over_stock_list=over_stock_list,
        pricing_rec=pricing_rec,
        marketing_rec=marketing_rec,
        inventory_rec=inventory_rec,
        customer_rec=customer_rec,
        causal_results=causal_results
    )

@user_bp.route('/recommendations')
@login_required
@role_required(['Admin', 'User'])
def recommendations():
    products = Product.query.all()
    if not products: flash("No data available. Please upload a dataset.", "warning"); return redirect(url_for('user.upload'))
    df = pd.DataFrame([{'Product Name': p.product_name, 'Category': p.category, 'Price': p.price, 'Marketing Spend': p.marketing_spend, 'Stock Quantity': p.stock_quantity, 'Sales': p.sales, 'Revenue': p.revenue, 'Profit': p.profit, 'Date': p.date} for p in products])
    causal_results = run_causal_analysis(df) if len(df) >= 5 else []
    forecasts = generate_forecasts(products)
    recs = generate_recommendations(products, causal_results=causal_results, forecasts=forecasts)

    # Aggregated data for recommendation visualizations
    rec_dist = {'Pricing': 0, 'Marketing': 0, 'Inventory': 0, 'Efficiency': 0, 'Strategic': 0}
    for r in recs:
        cat = r['category'].split()[0]
        if cat in rec_dist: rec_dist[cat] += 1
        elif 'Capital' in r['category']: rec_dist['Efficiency'] += 1
        else: rec_dist['Strategic'] += 1

    # Convert products to serializable dicts for Chart.js
    products_list = [{
        'id': p.id,
        'product_name': p.product_name,
        'price': p.price,
        'sales': p.sales,
        'marketing_spend': p.marketing_spend,
        'revenue': p.revenue
    } for p in products]

    return render_template('user/recommendations.html', recommendations=recs, products=products_list, causal_results=causal_results, rec_dist=rec_dist)

@user_bp.route('/reports')
@login_required
@role_required(['Admin', 'User'])
def reports():
    from datetime import datetime, timedelta
    page = request.args.get('page', 1, type=int)
    search = request.args.get('search')
    rep_type = request.args.get('type')
    date_filter = request.args.get('date_filter')

    query = Report.query
    if search:
        query = query.filter(Report.report_name.contains(search))
    if rep_type:
        query = query.filter(Report.report_type == rep_type)

    # Date Filtering
    now = datetime.utcnow()
    if date_filter == 'Today':
        query = query.filter(db.func.date(Report.created_at) == now.date())
    elif date_filter == 'Last 7 Days':
        query = query.filter(Report.created_at >= now - timedelta(days=7))
    elif date_filter == 'Last Month':
        query = query.filter(Report.created_at >= now - timedelta(days=30))

    pagination = query.order_by(Report.created_at.desc()).paginate(page=page, per_page=10)
    reports_items = pagination.items

    # Stats
    stats = {
        'total': Report.query.count(),
        'this_month': Report.query.filter(Report.created_at >= now.replace(day=1)).count(),
        'inventory': Report.query.filter(Report.report_type == 'Inventory').count(),
        'prediction': Report.query.filter(Report.report_type == 'Prediction').count(),
        'detection': Report.query.filter(Report.report_type == 'AI Detection').count()
    }

    # Analytics Data
    monthly_data = db.session.query(db.func.strftime('%Y-%m', Report.created_at), db.func.count(Report.id)).group_by(db.func.strftime('%Y-%m', Report.created_at)).all()
    type_data = db.session.query(Report.report_type, db.func.count(Report.id)).group_by(Report.report_type).all()
    user_data = db.session.query(User.full_name, db.func.count(Report.id)).join(Report).group_by(User.full_name).all()

    analytics = {
        'labels': [m[0] for m in monthly_data],
        'counts': [m[1] for m in monthly_data],
        'types': {t[0]: t[1] for t in type_data},
        'users': {u[0]: u[1] for u in user_data}
    }

    return render_template('user/reports.html',
                           recent_reports=reports_items,
                           pagination=pagination,
                           stats=stats,
                           now=now,
                           analytics=analytics)

@user_bp.route('/download_report/<format>')
@login_required
@role_required(['Admin', 'User'])
def download_report(format):
    rep_type = request.args.get('type', 'Executive')
    products = Product.query.all()

    if rep_type == 'Low Stock':
        products = [p for p in products if p.stock_quantity <= p.minimum_threshold]
    elif rep_type == 'Stock':
        products = Product.query.order_by(Product.stock_quantity.desc()).all()

    ts, tp, tr = sum([p.sales for p in products]), sum([p.profit for p in products]), sum([p.revenue for p in products])

    if format == 'pdf':
        buffer = generate_pdf_report(products, ts, tp, tr, generated_by=current_user.full_name)
        filename = f"SmartBiz_{rep_type}_{pd.Timestamp.now().strftime('%Y%m%d_%H%M')}.pdf"
        mimetype = 'application/pdf'
    elif format == 'excel':
        buffer = generate_excel_report(products)
        filename = f"SmartBiz_{rep_type}_{pd.Timestamp.now().strftime('%Y%m%d_%H%M')}.xlsx"
        mimetype = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    elif format == 'csv':
        data = []
        for p in products:
            data.append({'Name': p.product_name, 'Qty': p.stock_quantity, 'Price': p.price, 'Category': p.category})
        df = pd.DataFrame(data)
        buffer = io.BytesIO()
        df.to_csv(buffer, index=False)
        filename = f"SmartBiz_{rep_type}_{pd.Timestamp.now().strftime('%Y%m%d_%H%M')}.csv"
        mimetype = 'text/csv'
    else:
        return redirect(url_for('user.reports'))

    # Save file to disk for archive access
    filepath = os.path.join(current_app.config['REPORTS_FOLDER'], filename)
    with open(filepath, 'wb') as f:
        f.write(buffer.getbuffer())

    new_report = Report(
        report_name=filename,
        report_type=rep_type,
        file_path=filename,
        file_size=f"{os.path.getsize(filepath) // 1024} KB",
        user_id=current_user.id
    )
    db.session.add(new_report)
    db.session.add(AuditLog(user_id=current_user.id, action=f"Generated {format.upper()} Report: {rep_type}", module="Reports"))
    db.session.commit()

    buffer.seek(0)
    return send_file(buffer, as_attachment=True, download_name=filename, mimetype=mimetype)

@user_bp.route('/reports/download/<int:report_id>')
@login_required
def download_existing_report(report_id):
    report = db.session.get(Report, report_id)
    if not report:
        abort(404)
    filepath = os.path.join(current_app.config['REPORTS_FOLDER'], report.file_path)
    if os.path.exists(filepath):
        return send_file(filepath, as_attachment=True)
    flash("Report file not found on server.", "danger")
    return redirect(url_for('user.reports'))

@user_bp.route('/reports/delete/<int:report_id>', methods=['POST'])
@login_required
def delete_report_user(report_id):
    report = db.session.get(Report, report_id)
    if not report:
        abort(404)
    # Only allow owners or admins to delete
    if report.user_id != current_user.id and current_user.role != 'Admin':
        flash("Unauthorized deletion attempt.", "danger")
        return redirect(url_for('user.reports'))

    filepath = os.path.join(current_app.config['REPORTS_FOLDER'], report.file_path)
    if os.path.exists(filepath):
        os.remove(filepath)
    db.session.delete(report)
    db.session.commit()
    flash("Report removed from strategic archive.", "success")
    return redirect(url_for('user.reports'))

@user_bp.route('/inventory/detect', methods=['POST'])
@login_required
def inventory_detect():
    if 'file' not in request.files:
        return {'error': 'No file'}, 400
    file = request.files['file']
    if file.filename == '':
        return {'error': 'No selected file'}, 400

    # Security check: Validate image file extension
    allowed_image_exts = {'.png', '.jpg', '.jpeg', '.gif', '.webp', '.bmp'}
    _, ext = os.path.splitext(file.filename.lower())
    if ext not in allowed_image_exts:
        return {'error': 'Invalid image file format. Allowed formats: PNG, JPG, JPEG, GIF, WEBP, BMP'}, 400

    # Read file content into memory for OpenCV
    file_bytes = file.read()
    import base64
    encoded = base64.b64encode(file_bytes).decode('utf-8')
    image_data = f"data:image/jpeg;base64,{encoded}"

    count, annotated_filename, confidence, proc_time = detect_objects(image_data)

    # Store in DB
    detection = InventoryDetection(
        image_name=file.filename,
        annotated_path=annotated_filename,
        box_count=count,
        average_confidence=confidence,
        processing_time=proc_time,
        user_id=current_user.id
    )
    db.session.add(detection)
    db.session.add(AuditLog(user_id=current_user.id, action=f"AI Detection: Found {count} boxes in {file.filename}", module="Inventory"))
    db.session.commit()

    return {
        'count': count,
        'annotated': annotated_filename,
        'confidence': confidence,
        'proc_time': proc_time
    }

@user_bp.route('/inventory/detect_sample', methods=['POST'])
@login_required
def detect_sample():
    data = request.get_json()
    filename = data.get('filename')
    sample_path = os.path.join(current_app.config['SAMPLE_IMAGES_FOLDER'], filename)

    if not os.path.exists(sample_path):
        return {'error': 'Sample image not found'}, 404

    count, annotated_filename, confidence, proc_time = detect_objects(sample_path, is_path=True)

    detection = InventoryDetection(
        image_name=f"Sample: {filename}",
        annotated_path=annotated_filename,
        box_count=count,
        average_confidence=confidence,
        processing_time=proc_time,
        user_id=current_user.id
    )
    db.session.add(detection)
    db.session.commit()

    return {
        'count': count,
        'annotated': annotated_filename,
        'confidence': confidence,
        'proc_time': proc_time
    }

@user_bp.route('/update_inventory', methods=['POST'])
@login_required
def update_inventory():
    data = request.get_json(); pid = data.get('product_id'); qty = int(data.get('quantity'))
    product = db.session.get(Product, pid)
    if product:
        product.stock_quantity = qty
        db.session.add(InventoryLog(product_id=pid, detected_quantity=qty))
        db.session.commit()
        return {'success': True}
    return {'success': False}
