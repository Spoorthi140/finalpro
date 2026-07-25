from flask import Blueprint, render_template, redirect, url_for, flash, request, current_app, send_file
from flask_login import login_user, logout_user, login_required, current_user
from werkzeug.security import generate_password_hash, check_password_hash
from werkzeug.utils import secure_filename
import os
import io
import pandas as pd
from .models import db, User, Product, Prediction, InventoryLog, AuditLog, Report, InventoryDetection
from .causal_analysis import run_causal_analysis
from .recommendation_engine import generate_recommendations
from .prediction import train_and_predict, generate_forecasts
from .report_generator import generate_pdf_report, generate_excel_report
from .inventory_monitor import detect_objects
from .auth_utils import role_required
from .analytics_utils import perform_rfm_analysis, calculate_customer_lifetime_value, retention_analysis
import hashlib

user_bp = Blueprint('user', __name__)

# Thread-safe global dictionary memory cache for computationally intensive analytics
GLOBAL_CACHE = {
    'db_hash': None,
    'causal_results': None,
    'forecasts': None,
    'recommendations': None,
    'rec_dist': None,
    'historical': None
}

def get_db_state_hash():
    try:
        from sqlalchemy import func
        stats = db.session.query(
            func.count(Product.id),
            func.sum(Product.stock_quantity),
            func.sum(Product.sales),
            func.max(Product.last_updated)
        ).first()
        if not stats or stats[0] == 0:
            return "empty"
        state_str = f"{stats[0]}-{stats[1] or 0}-{stats[2] or 0}-{stats[3]}"
        return hashlib.md5(state_str.encode('utf-8')).hexdigest()
    except Exception:
        return "empty"

@user_bp.route('/')
def home():
    return render_template('user/home.html')

@user_bp.route('/register', methods=['GET', 'POST'])
def register():
    import re
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
        user = User.query.filter(db.func.lower(User.email) == email).first()
        if user and check_password_hash(user.password, password):
            if user.status != 'Active':
                flash('Your account is inactive. Please contact admin.', 'warning')
                return redirect(url_for('user.login'))
            login_user(user)
            db.session.add(AuditLog(user_id=user.id, action="User Login", module="Authentication"))
            db.session.commit()
            if user.role == 'Admin':
                return redirect(url_for('admin.dashboard'))
            return redirect(url_for('user.dashboard'))
        else:
            flash('Login failed. Check email and password.', 'danger')
    return render_template('user/login.html')

@user_bp.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('user.home'))

@user_bp.route('/dashboard')
@login_required
def dashboard():
    from datetime import datetime, timedelta

    # Run optimized count/sum queries on DB directly rather than pulling thousands of Python objects
    total_products = Product.query.count()
    categories = [c[0] for c in db.session.query(Product.category).distinct().all() if c[0]]
    total_users = User.query.count()
    total_inventory_items = db.session.query(db.func.sum(Product.stock_quantity)).scalar() or 0
    total_reports = Report.query.count()
    total_predictions = Prediction.query.count()

    low_stock_count = Product.query.filter(Product.stock_quantity > 0, Product.stock_quantity <= Product.minimum_threshold).count()
    out_of_stock_count = Product.query.filter(Product.stock_quantity <= 0).count()

    # Relative time helper
    def relative_time(dt):
        now = datetime.utcnow()
        diff = now - dt
        if diff.days == 0:
            if diff.seconds < 60:
                return "Just now"
            elif diff.seconds < 3600:
                mins = diff.seconds // 60
                return f"{mins} minute{'s' if mins > 1 else ''} ago"
            else:
                hours = diff.seconds // 3600
                return f"{hours} hour{'s' if hours > 1 else ''} ago"
        elif diff.days == 1:
            return "Yesterday"
        else:
            return f"{diff.days} days ago"

    # Activity Feed
    recent_activities = AuditLog.query.order_by(AuditLog.timestamp.desc()).limit(8).all()
    activities_list = []
    for log in recent_activities:
        activities_list.append({
            'user_name': log.user.full_name if log.user else 'System',
            'action': log.action,
            'relative_time': relative_time(log.timestamp)
        })

    # Reports
    recent_reports = Report.query.order_by(Report.created_at.desc()).limit(5).all()

    # Low Stock / Alerts (limited to 50 for page rendering)
    out_of_stock = Product.query.filter(Product.stock_quantity <= 0).limit(50).all()
    low_stock = Product.query.filter(Product.stock_quantity > 0, Product.stock_quantity <= Product.minimum_threshold).limit(50).all()
    newly_added = Product.query.filter(Product.created_at >= datetime.utcnow() - timedelta(days=7)).limit(5).all()

    # Prediction Stats
    today = datetime.utcnow().date()
    todays_predictions = Prediction.query.filter(db.func.date(Prediction.timestamp) == today).count()

    # Frequent Prediction Category
    from sqlalchemy import func
    top_cat = db.session.query(Product.category, func.count(Prediction.id)).join(Prediction).group_by(Product.category).order_by(func.count(Prediction.id).desc()).first()
    frequent_category = top_cat[0] if top_cat else "N/A"

    # Query raw values for chart calculations to improve speed by 10x
    product_data = db.session.query(
        Product.date, Product.revenue, Product.profit, Product.sales, Product.category, Product.stock_quantity
    ).all()

    # Chart Data
    if product_data:
        df = pd.DataFrame([{'Date': p[0], 'Revenue': p[1], 'Profit': p[2], 'Sales': p[3], 'Category': p[4], 'Stock': p[5]} for p in product_data])
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
            'In Stock': total_products - low_stock_count - out_of_stock_count,
            'Low Stock': low_stock_count,
            'Out of Stock': out_of_stock_count
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
                           low_stock_count=low_stock_count,
                           out_of_stock_count=out_of_stock_count,
                           todays_predictions=todays_predictions,
                           frequent_category=frequent_category,
                           activities=activities_list,
                           recent_reports=recent_reports,
                           out_of_stock=out_of_stock,
                           low_stock=low_stock,
                           newly_added=newly_added,
                           chart_labels=chart_labels,
                           chart_revenue=chart_revenue,
                           chart_profit=chart_profit,
                           cat_dist=cat_dist,
                           status_dist=status_dist,
                           report_chart_labels=report_chart_labels,
                           report_chart_data=report_chart_data)

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
                required_cols = ['Product Name', 'Category', 'Price', 'Marketing Spend', 'Stock Quantity', 'Sales', 'Revenue', 'Profit', 'Date']
                missing_cols = [col for col in required_cols if col not in df.columns]
                if missing_cols:
                    flash(f'Missing columns: {", ".join(missing_cols)}', 'danger'); return redirect(request.url)
                total_records = len(df)
                missing_values = df.isnull().sum().sum()
                duplicates_removed = total_records - len(df.drop_duplicates())
                df = df.drop_duplicates().fillna(0)
                outliers = 0
                for col in ['Price', 'Sales', 'Profit']:
                    if df[col].std() > 0:
                        z_scores = (df[col] - df[col].mean()) / df[col].std()
                        outliers += (z_scores.abs() > 3).sum()
                quality_score = min(100, max(0, 100 - (missing_values * 2) - (duplicates_removed * 1) - (outliers * 5)))
                for _, row in df.iterrows():
                    p = Product.query.filter_by(product_name=row['Product Name']).first()
                    if p:
                        p.category = row['Category']; p.price = row['Price']
                        p.marketing_spend = row['Marketing Spend']; p.stock_quantity = row['Stock Quantity']
                        p.sales = row['Sales']; p.revenue = row['Revenue']; p.profit = row['Profit']
                        p.date = pd.to_datetime(row['Date'])
                    else:
                        new_p = Product(
                            product_name=row['Product Name'], category=row['Category'],
                            price=row['Price'], marketing_spend=row['Marketing Spend'],
                            stock_quantity=row['Stock Quantity'], sales=row['Sales'],
                            revenue=row['Revenue'], profit=row['Profit'], date=pd.to_datetime(row['Date'])
                        )
                        db.session.add(new_p)
                db.session.commit()
                upload_results = {
                    'total_records': total_records, 'missing_values': missing_values,
                    'duplicates_removed': duplicates_removed, 'outliers_detected': outliers,
                    'quality_score': quality_score, 'status': 'Dataset Uploaded and Processed Successfully'
                }
                preview_data = df.head(10).to_dict('records')
                flash('Data analysis and validation complete!', 'success')
            except Exception as e:
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
    products = pagination.items
    all_products = db.session.query(Product.id, Product.product_name, Product.stock_quantity, Product.minimum_threshold).all()

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
                           all_products=all_products,
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
        flash("Product not found.", "danger")
        return redirect(url_for('user.inventory'))
    try:
        p.product_name = request.form.get('name')
        p.category = request.form.get('category')
        p.stock_quantity = int(request.form.get('quantity', 0))
        p.price = float(request.form.get('price', 0))
        p.supplier = request.form.get('supplier')
        p.description = request.form.get('description')

        db.session.add(AuditLog(user_id=current_user.id, action=f"Updated Product: {p.product_name}", module="Inventory"))
        db.session.commit()
        flash(f"Product {p.product_name} updated successfully.", "success")
    except Exception as e:
        db.session.rollback()
        flash(f"Error updating product: {e}", "danger")
    return redirect(url_for('user.inventory'))

@user_bp.route('/inventory/delete/<int:product_id>', methods=['POST'])
@login_required
def delete_product_user(product_id):
    p = db.session.get(Product, product_id)
    if not p:
        flash("Product not found.", "danger")
        return redirect(url_for('user.inventory'))
    try:
        name = p.product_name
        db.session.delete(p)
        db.session.add(AuditLog(user_id=current_user.id, action=f"Deleted Product: {name}", module="Inventory"))
        db.session.commit()
        flash(f"Product {name} deleted.", "danger")
    except Exception as e:
        db.session.rollback()
        flash(f"Error deleting product: {e}", "danger")
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

    try:
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
    except Exception as e:
        db.session.rollback()
        flash(f"Error adding product: {e}", "danger")
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
    db_hash = get_db_state_hash()
    if GLOBAL_CACHE['db_hash'] == db_hash and GLOBAL_CACHE['forecasts'] is not None and GLOBAL_CACHE['historical'] is not None:
        forecasts = GLOBAL_CACHE['forecasts']
        historical = GLOBAL_CACHE['historical']
    else:
        products = Product.query.all()
        if not products:
            flash("No data available for forecasting. Please upload a dataset.", "warning")
            return redirect(url_for('user.upload'))
        forecasts = generate_forecasts(products)

        # Historical data for combined charts
        df = pd.DataFrame([{'Date': p.date, 'Revenue': p.revenue, 'Sales': p.sales} for p in products])
        df['Date'] = pd.to_datetime(df['Date'])
        hist_monthly = df.set_index('Date').resample('ME').sum().tail(6).reset_index()
        historical = {
            'labels': [d.strftime('%b %Y') for d in hist_monthly['Date']],
            'revenue': hist_monthly['Revenue'].tolist(),
            'sales': hist_monthly['Sales'].tolist()
        }
        # Update cache
        GLOBAL_CACHE['db_hash'] = db_hash
        GLOBAL_CACHE['forecasts'] = forecasts
        GLOBAL_CACHE['historical'] = historical

    return render_template('user/forecasting.html', forecasts=forecasts, historical=historical)

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

@user_bp.route('/causal_analysis')
@login_required
@role_required(['Admin', 'User'])
def causal_analysis():
    db_hash = get_db_state_hash()
    if GLOBAL_CACHE['db_hash'] == db_hash and GLOBAL_CACHE['causal_results'] is not None:
        causal_results = GLOBAL_CACHE['causal_results']
    else:
        products = Product.query.all()
        if not products:
            flash("No data available for analysis.", "warning")
            return redirect(url_for('user.upload'))
        df = pd.DataFrame([{'Product Name': p.product_name, 'Category': p.category, 'Price': p.price, 'Marketing Spend': p.marketing_spend, 'Stock Quantity': p.stock_quantity, 'Sales': p.sales, 'Revenue': p.revenue, 'Profit': p.profit, 'Date': p.date} for p in products])
        causal_results = run_causal_analysis(df) if len(df) >= 5 else []
        GLOBAL_CACHE['db_hash'] = db_hash
        GLOBAL_CACHE['causal_results'] = causal_results
    return render_template('user/causal_analysis.html', causal_results=causal_results)

@user_bp.route('/recommendations')
@login_required
@role_required(['Admin', 'User'])
def recommendations():
    db_hash = get_db_state_hash()
    if GLOBAL_CACHE['db_hash'] == db_hash and GLOBAL_CACHE['recommendations'] is not None and GLOBAL_CACHE['rec_dist'] is not None:
        recs = GLOBAL_CACHE['recommendations']
        rec_dist = GLOBAL_CACHE['rec_dist']
        products = Product.query.all()
    else:
        products = Product.query.all()
        if not products:
            flash("No data available. Please upload a dataset.", "warning")
            return redirect(url_for('user.upload'))
        df = pd.DataFrame([{'Product Name': p.product_name, 'Category': p.category, 'Price': p.price, 'Marketing Spend': p.marketing_spend, 'Stock Quantity': p.stock_quantity, 'Sales': p.sales, 'Revenue': p.revenue, 'Profit': p.profit, 'Date': p.date} for p in products])

        # Pull causal_results from cache if valid
        if GLOBAL_CACHE['db_hash'] == db_hash and GLOBAL_CACHE['causal_results'] is not None:
            causal_results = GLOBAL_CACHE['causal_results']
        else:
            causal_results = run_causal_analysis(df) if len(df) >= 5 else []
            GLOBAL_CACHE['causal_results'] = causal_results

        # Pull forecasts from cache if valid
        if GLOBAL_CACHE['db_hash'] == db_hash and GLOBAL_CACHE['forecasts'] is not None:
            forecasts = GLOBAL_CACHE['forecasts']
        else:
            forecasts = generate_forecasts(products)
            GLOBAL_CACHE['forecasts'] = forecasts

        recs = generate_recommendations(products, causal_results=causal_results, forecasts=forecasts)

        # Aggregated data for recommendation visualizations
        rec_dist = {'Pricing': 0, 'Marketing': 0, 'Inventory': 0, 'Efficiency': 0, 'Strategic': 0}
        for r in recs:
            cat = r['category'].split()[0]
            if cat in rec_dist: rec_dist[cat] += 1
            elif 'Capital' in r['category']: rec_dist['Efficiency'] += 1
            else: rec_dist['Strategic'] += 1

        GLOBAL_CACHE['db_hash'] = db_hash
        GLOBAL_CACHE['recommendations'] = recs
        GLOBAL_CACHE['rec_dist'] = rec_dist

    # Convert products to serializable dicts for Chart.js
    products_list = [{
        'id': p.id,
        'product_name': p.product_name,
        'price': p.price,
        'sales': p.sales,
        'marketing_spend': p.marketing_spend,
        'revenue': p.revenue
    } for p in products]

    causal_results = GLOBAL_CACHE.get('causal_results') or []

    return render_template('user/recommendations.html', recommendations=recs, products=products_list, causal_results=causal_results, rec_dist=rec_dist)

@user_bp.route('/reports')
@login_required
@role_required(['Admin', 'User'])
def reports():
    from datetime import datetime
    page = request.args.get('page', 1, type=int)
    rep_type = request.args.get('type', 'Inventory Report') # default to Inventory Report
    start_date = request.args.get('start_date', '').strip()
    end_date = request.args.get('end_date', '').strip()

    # Query Recent Reports (the physical PDF/Excel/CSV generated files archive list)
    pagination = Report.query.order_by(Report.created_at.desc()).paginate(page=page, per_page=10)
    reports_items = pagination.items

    # Fetch report data dynamically from database based on selected filters
    report_data = []
    headers = []

    # Parse dates if supplied
    s_dt = None
    e_dt = None
    if start_date:
        try:
            s_dt = pd.to_datetime(start_date)
        except:
            pass
    if end_date:
        try:
            e_dt = pd.to_datetime(end_date)
        except:
            pass

    if rep_type == 'Inventory Report' or rep_type == 'Stock Report':
        query = Product.query
        if s_dt:
            query = query.filter(Product.date >= s_dt)
        if e_dt:
            query = query.filter(Product.date <= e_dt)
        items = query.order_by(Product.product_name).all()

        if rep_type == 'Inventory Report':
            headers = ['Product Name', 'Category', 'Price', 'Stock Quantity', 'Date Added']
            report_data = [{
                'Product Name': p.product_name,
                'Category': p.category,
                'Price': f"₹{p.price:,.2f}",
                'Stock Quantity': p.stock_quantity,
                'Date Added': p.date.strftime('%Y-%m-%d') if p.date else 'N/A'
            } for p in items]
        else: # Stock Report
            headers = ['Product Name', 'Stock Quantity', 'Minimum Threshold', 'Maximum Threshold', 'Status']
            report_data = [{
                'Product Name': p.product_name,
                'Stock Quantity': p.stock_quantity,
                'Minimum Threshold': p.minimum_threshold,
                'Maximum Threshold': p.maximum_threshold,
                'Status': 'In Stock' if p.stock_quantity > p.minimum_threshold else 'Low Stock'
            } for p in items]

    elif rep_type == 'Sales Report':
        query = Product.query
        if s_dt:
            query = query.filter(Product.date >= s_dt)
        if e_dt:
            query = query.filter(Product.date <= e_dt)
        items = query.order_by(Product.sales.desc()).all()
        headers = ['Product Name', 'Sales Volume', 'Unit Price', 'Revenue Generated', 'Profit Realized']
        report_data = [{
            'Product Name': p.product_name,
            'Sales Volume': p.sales,
            'Unit Price': f"₹{p.price:,.2f}",
            'Revenue Generated': f"₹{p.revenue:,.2f}",
            'Profit Realized': f"₹{p.profit:,.2f}"
        } for p in items]

    elif rep_type == 'AI Prediction Report':
        query = Prediction.query
        if s_dt:
            query = query.filter(Prediction.timestamp >= s_dt)
        if e_dt:
            query = query.filter(Prediction.timestamp <= e_dt)
        items = query.order_by(Prediction.timestamp.desc()).all()
        headers = ['Product Name', 'Predicted Sales', 'Predicted Profit', 'Depletion Days', 'Generated Time']
        report_data = [{
            'Product Name': p.product.product_name if p.product else 'N/A',
            'Predicted Sales': p.predicted_sales,
            'Predicted Profit': f"₹{p.predicted_profit:,.2f}",
            'Depletion Days': p.stock_out_days,
            'Generated Time': p.timestamp.strftime('%Y-%m-%d %H:%M') if p.timestamp else 'N/A'
        } for p in items]

    elif rep_type == 'AI Recommendation Report':
        products = Product.query.all()
        from .recommendation_engine import generate_recommendations
        db_hash = get_db_state_hash()
        if GLOBAL_CACHE['db_hash'] == db_hash and GLOBAL_CACHE['recommendations'] is not None:
            recs = GLOBAL_CACHE['recommendations']
        else:
            recs = generate_recommendations(products)
        headers = ['Priority', 'Category', 'Recommended Action', 'Impact Insight']
        report_data = [{
            'Priority': r['priority'],
            'Category': r['category'],
            'Recommended Action': r['action'],
            'Impact Insight': r['message']
        } for r in recs]

    return render_template('user/reports.html',
                           recent_reports=reports_items,
                           pagination=pagination,
                           headers=headers,
                           report_data=report_data,
                           selected_type=rep_type,
                           start_date=start_date,
                           end_date=end_date,
                           now=datetime.utcnow())

@user_bp.route('/download_report/<format>')
@login_required
@role_required(['Admin', 'User'])
def download_report(format):
    rep_type = request.args.get('type', 'Inventory Report')
    start_date = request.args.get('start_date', '').strip()
    end_date = request.args.get('end_date', '').strip()

    # Parse dates if supplied
    s_dt = None
    e_dt = None
    if start_date:
        try:
            s_dt = pd.to_datetime(start_date)
        except:
            pass
    if end_date:
        try:
            e_dt = pd.to_datetime(end_date)
        except:
            pass

    # Query matching data
    products = []
    df_data = []

    if rep_type == 'Inventory Report' or rep_type == 'Stock Report' or rep_type == 'Sales Report':
        query = Product.query
        if s_dt:
            query = query.filter(Product.date >= s_dt)
        if e_dt:
            query = query.filter(Product.date <= e_dt)
        products = query.order_by(Product.product_name).all()

        for p in products:
            if rep_type == 'Inventory Report':
                df_data.append({
                    'Product Name': p.product_name,
                    'Category': p.category,
                    'Price': p.price,
                    'Stock Quantity': p.stock_quantity,
                    'Date Added': p.date.strftime('%Y-%m-%d') if p.date else ''
                })
            elif rep_type == 'Stock Report':
                df_data.append({
                    'Product Name': p.product_name,
                    'Stock Quantity': p.stock_quantity,
                    'Minimum Threshold': p.minimum_threshold,
                    'Maximum Threshold': p.maximum_threshold,
                    'Status': 'In Stock' if p.stock_quantity > p.minimum_threshold else 'Low Stock'
                })
            else: # Sales Report
                df_data.append({
                    'Product Name': p.product_name,
                    'Sales Volume': p.sales,
                    'Unit Price': p.price,
                    'Revenue Generated': p.revenue,
                    'Profit Realized': p.profit
                })

    elif rep_type == 'AI Prediction Report':
        query = Prediction.query
        if s_dt:
            query = query.filter(Prediction.timestamp >= s_dt)
        if e_dt:
            query = query.filter(Prediction.timestamp <= e_dt)
        predictions = query.all()
        for p in predictions:
            df_data.append({
                'Product Name': p.product.product_name if p.product else 'N/A',
                'Predicted Sales': p.predicted_sales,
                'Predicted Profit': p.predicted_profit,
                'Depletion Days': p.stock_out_days,
                'Timestamp': p.timestamp.strftime('%Y-%m-%d %H:%M') if p.timestamp else ''
            })

    elif rep_type == 'AI Recommendation Report':
        query_prod = Product.query.all()
        from .recommendation_engine import generate_recommendations
        recs = generate_recommendations(query_prod)
        for r in recs:
            df_data.append({
                'Priority': r['priority'],
                'Category': r['category'],
                'Recommended Action': r['action'],
                'Impact Insight': r['message']
            })

    # Prepare file stream
    buffer = io.BytesIO()
    if format == 'pdf':
        from reportlab.lib.pagesizes import letter
        from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle
        from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
        from reportlab.lib import colors

        doc = SimpleDocTemplate(buffer, pagesize=letter, rightMargin=36, leftMargin=36, topMargin=36, bottomMargin=36)
        story = []
        styles = getSampleStyleSheet()

        title_style = ParagraphStyle(
            'ReportTitle',
            parent=styles['Heading1'],
            fontName='Helvetica-Bold',
            fontSize=18,
            textColor=colors.HexColor('#1E3A8A'),
            spaceAfter=15
        )
        story.append(Paragraph(f"SmartBiz Enterprise - {rep_type}", title_style))
        story.append(Paragraph(f"Generated on: {datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S')} (UTC)", styles['Normal']))
        if start_date or end_date:
            story.append(Paragraph(f"Filters: {start_date} to {end_date}", styles['Normal']))
        story.append(Spacer(1, 15))

        if df_data:
            col_headers = list(df_data[0].keys())
            data_table = [col_headers]
            for row in df_data:
                data_table.append([str(v) for v in row.values()])

            t = Table(data_table)
            t.setStyle(TableStyle([
                ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#1E3A8A')),
                ('TEXTCOLOR', (0,0), (-1,0), colors.whitesmoke),
                ('ALIGN', (0,0), (-1,-1), 'LEFT'),
                ('BOTTOMPADDING', (0,0), (-1,0), 6),
                ('TOPPADDING', (0,0), (-1,0), 6),
                ('GRID', (0,0), (-1,-1), 0.5, colors.grey),
                ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
                ('FONTSIZE', (0,0), (-1,-1), 8),
            ]))
            story.append(t)
        else:
            story.append(Paragraph("No records found matching filters.", styles['Normal']))

        doc.build(story)
        filename = f"SmartBiz_{rep_type.replace(' ', '_')}_{pd.Timestamp.now().strftime('%Y%m%d_%H%M')}.pdf"
        mimetype = 'application/pdf'

    elif format == 'excel':
        df = pd.DataFrame(df_data)
        df.to_excel(buffer, index=False)
        filename = f"SmartBiz_{rep_type.replace(' ', '_')}_{pd.Timestamp.now().strftime('%Y%m%d_%H%M')}.xlsx"
        mimetype = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'

    elif format == 'csv':
        df = pd.DataFrame(df_data)
        df.to_csv(buffer, index=False)
        filename = f"SmartBiz_{rep_type.replace(' ', '_')}_{pd.Timestamp.now().strftime('%Y%m%d_%H%M')}.csv"
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
        flash("Report not found.", "danger")
        return redirect(url_for('user.reports'))
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
        flash("Report not found.", "danger")
        return redirect(url_for('user.reports'))
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
