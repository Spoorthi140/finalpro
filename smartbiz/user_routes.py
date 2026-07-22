from flask import Blueprint, render_template, redirect, url_for, flash, request, current_app, send_file
from flask_login import login_user, logout_user, login_required, current_user
from werkzeug.security import generate_password_hash, check_password_hash
from werkzeug.utils import secure_filename
import os
import io
import pandas as pd
from .models import db, User, Product, Prediction, InventoryLog, AuditLog, Company, Report, InventoryDetection
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

    # Activity Feed
    recent_activities = AuditLog.query.order_by(AuditLog.timestamp.desc()).limit(8).all()

    # Reports
    recent_reports = Report.query.order_by(Report.created_at.desc()).limit(5).all()

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
    p = Product.query.get_or_404(product_id)
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
    p = Product.query.get_or_404(product_id)
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

    return render_template('user/forecasting.html', forecasts=forecasts, historical=historical)

@user_bp.route('/customer_intelligence')
@login_required
@role_required(['Admin', 'User'])
def customer_intelligence():
    segments = perform_rfm_analysis(); clv = calculate_customer_lifetime_value(); retention = retention_analysis()
    return render_template('user/customer_intelligence.html', segments=segments, clv=clv, retention=retention)

@user_bp.route('/company_profile', methods=['GET', 'POST'])
@login_required
@role_required(['Admin', 'User'])
def company_profile():
    company = Company.query.first()
    if request.method == 'POST':
        if not company:
            company = Company()
        company.name = request.form.get('name')
        company.industry = request.form.get('industry')
        company.business_type = request.form.get('business_type')
        company.address = request.form.get('address')
        company.contact_number = request.form.get('contact_number')
        company.branch_count = int(request.form.get('branch_count', 1))

        db.session.add(company)
        db.session.add(AuditLog(user_id=current_user.id, action="Updated Company Profile", module="Company"))
        db.session.commit()
        flash("Company profile updated successfully.", "success")
        return redirect(url_for('user.company_profile'))

    return render_template('user/company_profile.html', company=company)

@user_bp.route('/executive_dashboard')
@login_required
@role_required(['Admin', 'User', 'Admin'])
def executive_dashboard():
    products = Product.query.all()
    # Logic for Executive Insights
    health_score = 85 # Mock logic for demo
    if not products:
        health_score = 0

    # Risk Assessment
    risks = {
        'revenue': 'Low' if sum([p.revenue for p in products]) > 10000 else 'Medium',
        'inventory': 'High' if Product.query.filter(Product.stock_quantity <= Product.minimum_threshold).count() > 5 else 'Low',
        'demand': 'Stable',
        'churn': 'Low'
    }

    return render_template('user/executive_dashboard.html', health_score=health_score, risks=risks)

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
    products = Product.query.all()
    if not products: flash("No data available for analysis.", "warning"); return redirect(url_for('user.upload'))
    df = pd.DataFrame([{'Product Name': p.product_name, 'Category': p.category, 'Price': p.price, 'Marketing Spend': p.marketing_spend, 'Stock Quantity': p.stock_quantity, 'Sales': p.sales, 'Revenue': p.revenue, 'Profit': p.profit, 'Date': p.date} for p in products])
    causal_results = run_causal_analysis(df) if len(df) >= 5 else []
    return render_template('user/causal_analysis.html', causal_results=causal_results)

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
        buffer = generate_pdf_report(products, ts, tp, tr)
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
    report = Report.query.get_or_404(report_id)
    filepath = os.path.join(current_app.config['REPORTS_FOLDER'], report.file_path)
    if os.path.exists(filepath):
        return send_file(filepath, as_attachment=True)
    flash("Report file not found on server.", "danger")
    return redirect(url_for('user.reports'))

@user_bp.route('/reports/delete/<int:report_id>', methods=['POST'])
@login_required
def delete_report_user(report_id):
    report = Report.query.get_or_404(report_id)
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
    product = Product.query.get(pid)
    if product:
        product.stock_quantity = qty
        db.session.add(InventoryLog(product_id=pid, detected_quantity=qty))
        db.session.commit()
        return {'success': True}
    return {'success': False}
