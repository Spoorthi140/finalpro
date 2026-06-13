from flask import Blueprint, render_template, redirect, url_for, flash, request, current_app, send_file
from flask_login import login_user, logout_user, login_required, current_user
from werkzeug.security import generate_password_hash, check_password_hash
from werkzeug.utils import secure_filename
import os
import pandas as pd
from .models import db, User, Product, Prediction, InventoryLog, AuditLog, Company
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
    if current_user.is_authenticated:
        return redirect(url_for('user.dashboard'))
    if request.method == 'POST':
        username = request.form.get('username')
        email = request.form.get('email')
        password = request.form.get('password')

        user_count = User.query.count()
        role = 'Super Admin' if user_count == 0 else 'Viewer'

        user_exists = User.query.filter_by(email=email).first()
        if user_exists:
            flash('Email already exists.', 'danger')
            return redirect(url_for('user.register'))

        new_user = User(
            username=username,
            email=email,
            password=generate_password_hash(password, method='pbkdf2:sha256'),
            role=role
        )
        db.session.add(new_user)
        db.session.commit()
        flash('Registration successful! Please login.', 'success')
        return redirect(url_for('user.login'))
    return render_template('user/register.html')

@user_bp.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('user.dashboard'))
    if request.method == 'POST':
        email = request.form.get('email')
        password = request.form.get('password')
        user = User.query.filter_by(email=email).first()

        if user and check_password_hash(user.password, password):
            login_user(user)
            return redirect(url_for('user.dashboard'))
        else:
            flash('Login failed. Check email and password.', 'danger')
    return render_template('user/login.html')

@user_bp.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('user.home'))

@user_bp.route('/company/profile', methods=['GET', 'POST'])
@login_required
@role_required(['Super Admin', 'Business Admin', 'Manager'])
def company_profile():
    company = Company.query.first()
    if request.method == 'POST':
        name = request.form.get('name')
        industry = request.form.get('industry')
        business_type = request.form.get('business_type')
        address = request.form.get('address')
        contact_number = request.form.get('contact_number')
        branch_count = int(request.form.get('branch_count', 1))

        if company:
            company.name = name
            company.industry = industry
            company.business_type = business_type
            company.address = address
            company.contact_number = contact_number
            company.branch_count = branch_count
        else:
            company = Company(
                name=name,
                industry=industry,
                business_type=business_type,
                address=address,
                contact_number=contact_number,
                branch_count=branch_count
            )
            db.session.add(company)

        db.session.commit()
        flash('Company profile updated successfully.', 'success')
        return redirect(url_for('user.company_profile'))

    return render_template('user/company_profile.html', company=company)

@user_bp.route('/executive')
@login_required
@role_required(['Super Admin', 'Business Admin', 'Manager'])
def executive_dashboard():
    products = Product.query.all()
    total_revenue = sum([p.revenue for p in products])
    total_profit = sum([p.profit for p in products])

    # Mock Risk Analysis
    health_score = 78
    risks = [
        {'name': 'Revenue Risk', 'level': 'Low', 'color': 'success'},
        {'name': 'Inventory Risk', 'level': 'Medium', 'color': 'warning'},
        {'name': 'Demand Risk', 'level': 'Low', 'color': 'success'},
        {'name': 'Churn Risk', 'level': 'High', 'color': 'danger'}
    ]

    return render_template('user/executive_dashboard.html',
                           total_revenue=total_revenue,
                           total_profit=total_profit,
                           health_score=health_score,
                           risks=risks)

@user_bp.route('/dashboard')
@login_required
def dashboard():
    products = Product.query.all()
    total_sales = sum([p.sales for p in products])
    total_profit = sum([p.profit for p in products])
    total_revenue = sum([p.revenue for p in products])
    total_inventory = sum([p.stock_quantity for p in products])
    inventory_value = sum([p.stock_quantity * p.price for p in products])

    low_stock_products = Product.query.filter(Product.stock_quantity < 10).all()

    # Growth Rate Mock Calculation (comparing latest to average)
    avg_sales = total_sales / len(products) if products else 0
    growth_rate = 12.5 # Mock value

    return render_template('user/dashboard.html',
                           products=products,
                           total_sales=total_sales,
                           total_profit=total_profit,
                           total_revenue=total_revenue,
                           total_inventory=total_inventory,
                           inventory_value=inventory_value,
                           growth_rate=growth_rate,
                           low_stock_count=len(low_stock_products))

@user_bp.route('/upload', methods=['GET', 'POST'])
@login_required
@role_required(['Super Admin', 'Business Admin', 'Manager'])
def upload():
    upload_results = None
    preview_data = []

    if request.method == 'POST':
        if 'file' not in request.files:
            flash('No file part', 'danger')
            return redirect(request.url)

        file = request.files['file']
        if file.filename == '':
            flash('No selected file', 'danger')
            return redirect(request.url)

        if file and (file.filename.endswith('.csv') or file.filename.endswith('.xlsx')):
            filename = secure_filename(file.filename)
            filepath = os.path.join(current_app.config['UPLOAD_FOLDER'], filename)
            file.save(filepath)

            try:
                if filename.endswith('.csv'):
                    df = pd.read_csv(filepath)
                else:
                    df = pd.read_excel(filepath)

                required_cols = ['Product Name', 'Category', 'Price', 'Marketing Spend', 'Stock Quantity', 'Sales', 'Revenue', 'Profit', 'Date']

                # Validation
                missing_cols = [col for col in required_cols if col not in df.columns]
                if missing_cols:
                    flash(f'Missing columns: {", ".join(missing_cols)}', 'danger')
                    return redirect(request.url)

                total_records = len(df)
                missing_values = df.isnull().sum().sum()
                duplicates_removed = total_records - len(df.drop_duplicates())
                df = df.drop_duplicates().fillna(0)

                # Outlier detection (simple Z-score > 3)
                outliers = 0
                for col in ['Price', 'Sales', 'Profit']:
                    if df[col].std() > 0:
                        z_scores = (df[col] - df[col].mean()) / df[col].std()
                        outliers += (z_scores.abs() > 3).sum()

                # Quality Score Calculation
                quality_score = max(0, 100 - (missing_values * 2) - (duplicates_removed * 1) - (outliers * 5))
                quality_score = min(100, quality_score)

                for _, row in df.iterrows():
                    product = Product.query.filter_by(product_name=row['Product Name']).first()
                    if product:
                        product.category = row['Category']
                        product.price = row['Price']
                        product.marketing_spend = row['Marketing Spend']
                        product.stock_quantity = row['Stock Quantity']
                        product.sales = row['Sales']
                        product.revenue = row['Revenue']
                        product.profit = row['Profit']
                        product.date = pd.to_datetime(row['Date'])
                    else:
                        new_product = Product(
                            product_name=row['Product Name'],
                            category=row['Category'],
                            price=row['Price'],
                            marketing_spend=row['Marketing Spend'],
                            stock_quantity=row['Stock Quantity'],
                            sales=row['Sales'],
                            revenue=row['Revenue'],
                            profit=row['Profit'],
                            date=pd.to_datetime(row['Date'])
                        )
                        db.session.add(new_product)

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
                flash('Data analysis and validation complete!', 'success')

            except Exception as e:
                flash(f'Error processing file: {str(e)}', 'danger')
                return redirect(request.url)
        else:
            flash('Only CSV and XLSX files are allowed.', 'danger')
            return redirect(request.url)

    return render_template('user/upload.html',
                           upload_results=upload_results,
                           preview_data=preview_data)

@user_bp.route('/inventory')
@login_required
@role_required(['Super Admin', 'Business Admin', 'Manager'])
def inventory():
    products = Product.query.all()
    return render_template('user/inventory.html', products=products)

@user_bp.route('/simulator', methods=['GET', 'POST'])
@login_required
@role_required(['Super Admin', 'Business Admin', 'Manager'])
def simulator():
    products = Product.query.all()
    simulation_result = None
    if request.method == 'POST':
        price_change = float(request.form.get('price_change', 0)) / 100
        marketing_change = float(request.form.get('marketing_change', 0)) / 100

        current_revenue = sum([p.revenue for p in products])
        current_profit = sum([p.profit for p in products])
        current_sales = sum([p.sales for p in products])

        # Simple Simulation Logic based on causal heuristics
        # Price up -> Sales down (Elasticity approx -1.5)
        # Marketing up -> Sales up (Elasticity approx 0.5)

        simulated_sales = current_sales * (1 - 1.5 * price_change) * (1 + 0.5 * marketing_change)
        simulated_revenue = current_revenue * (1 + price_change) * (simulated_sales / current_sales if current_sales > 0 else 1)
        simulated_profit = simulated_revenue * (current_profit / current_revenue if current_revenue > 0 else 0.2)

        simulation_result = {
            'current_sales': round(current_sales, 2),
            'current_revenue': round(current_revenue, 2),
            'current_profit': round(current_profit, 2),
            'simulated_sales': round(simulated_sales, 2),
            'simulated_revenue': round(simulated_revenue, 2),
            'simulated_profit': round(simulated_profit, 2),
            'sales_impact': round(((simulated_sales - current_sales) / current_sales * 100), 2) if current_sales > 0 else 0,
            'revenue_impact': round(((simulated_revenue - current_revenue) / current_revenue * 100), 2) if current_revenue > 0 else 0,
            'profit_impact': round(((simulated_profit - current_profit) / current_profit * 100), 2) if current_profit > 0 else 0,
        }

    return render_template('user/simulator.html', simulation=simulation_result)

@user_bp.route('/forecasting')
@login_required
@role_required(['Super Admin', 'Business Admin', 'Manager', 'Analyst'])
def forecasting():
    products = Product.query.all()
    forecasts = generate_forecasts(products)
    return render_template('user/forecasting.html', forecasts=forecasts)

@user_bp.route('/customer_intelligence')
@login_required
@role_required(['Super Admin', 'Business Admin', 'Manager', 'Analyst'])
def customer_intelligence():
    segments = perform_rfm_analysis()
    clv = calculate_customer_lifetime_value()
    retention = retention_analysis()
    return render_template('user/customer_intelligence.html', segments=segments, clv=clv, retention=retention)

@user_bp.route('/prediction', methods=['GET', 'POST'])
@login_required
@role_required(['Super Admin', 'Business Admin', 'Manager', 'Analyst'])
def prediction():
    prediction_result = None
    if request.method == 'POST':
        price = float(request.form.get('price', 0))
        marketing = float(request.form.get('marketing', 0))
        stock = float(request.form.get('stock', 0))

        products = Product.query.all()
        prediction_result = train_and_predict(products, price, marketing, stock)

        if prediction_result:
            new_pred = Prediction(
                product_id=products[0].id if products else None,
                predicted_sales=prediction_result['predicted_sales'],
                predicted_profit=prediction_result['predicted_profit'],
                stock_out_days=prediction_result['stock_out_days']
            )
            db.session.add(new_pred)
            db.session.commit()
        else:
            flash("Insufficient data to train prediction models.", "warning")

    return render_template('user/prediction.html', prediction=prediction_result)

@user_bp.route('/causal_analysis')
@login_required
@role_required(['Super Admin', 'Business Admin', 'Manager', 'Analyst'])
def causal_analysis():
    products = Product.query.all()
    if not products:
        flash("No data available for analysis.", "warning")
        return redirect(url_for('user.upload'))

    data = {
        'Product Name': [p.product_name for p in products],
        'Category': [p.category for p in products],
        'Price': [p.price for p in products],
        'Marketing Spend': [p.marketing_spend for p in products],
        'Stock Quantity': [p.stock_quantity for p in products],
        'Sales': [p.sales for p in products],
        'Revenue': [p.revenue for p in products],
        'Profit': [p.profit for p in products],
        'Date': [p.date for p in products]
    }
    df = pd.DataFrame(data)

    if len(df) < 5:
        flash("Need at least 5 products for a reliable causal analysis.", "info")
        causal_results = []
    else:
        causal_results = run_causal_analysis(df)

    return render_template('user/causal_analysis.html', causal_results=causal_results)

@user_bp.route('/recommendations')
@login_required
@role_required(['Super Admin', 'Business Admin', 'Manager', 'Analyst'])
def recommendations():
    products = Product.query.all()
    recs = generate_recommendations(products)
    return render_template('user/recommendations.html', recommendations=recs)

@user_bp.route('/reports')
@login_required
@role_required(['Super Admin', 'Business Admin', 'Manager', 'Analyst'])
def reports():
    return render_template('user/reports.html')

@user_bp.route('/download_report/<format>')
@login_required
@role_required(['Super Admin', 'Business Admin', 'Manager', 'Analyst'])
def download_report(format):
    products = Product.query.all()
    total_sales = sum([p.sales for p in products])
    total_profit = sum([p.profit for p in products])
    total_revenue = sum([p.revenue for p in products])

    if format == 'pdf':
        buffer = generate_pdf_report(products, total_sales, total_profit, total_revenue)
        return send_file(buffer, as_attachment=True, download_name="smartbiz_report.pdf", mimetype='application/pdf')
    elif format == 'excel':
        buffer = generate_excel_report(products)
        return send_file(buffer, as_attachment=True, download_name="smartbiz_report.xlsx", mimetype='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')

    flash("Invalid report format.", "danger")
    return redirect(url_for('user.reports'))

@user_bp.route('/process_inventory', methods=['POST'])
@login_required
def process_inventory():
    data = request.get_json()
    image_data = data.get('image')
    count, processed_image = detect_objects(image_data)
    return {'count': count, 'processed_image': processed_image}

@user_bp.route('/update_inventory', methods=['POST'])
@login_required
def update_inventory():
    data = request.get_json()
    product_id = data.get('product_id')
    quantity = int(data.get('quantity'))

    product = Product.query.get(product_id)
    if product:
        product.stock_quantity = quantity
        log = InventoryLog(product_id=product_id, detected_quantity=quantity)
        db.session.add(log)
        db.session.commit()
        return {'success': True}
    return {'success': False}
