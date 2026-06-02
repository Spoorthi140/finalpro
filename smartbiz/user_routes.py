from flask import Blueprint, render_template, redirect, url_for, flash, request, current_app, send_file
from flask_login import login_user, logout_user, login_required, current_user
from werkzeug.security import generate_password_hash, check_password_hash
from werkzeug.utils import secure_filename
import os
import pandas as pd
from models import db, User, Product, Prediction, InventoryLog
from causal_analysis import run_causal_analysis
from recommendation_engine import generate_recommendations
from prediction import train_and_predict
from report_generator import generate_pdf_report
from inventory_monitor import detect_objects

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
        role = 'admin' if user_count == 0 else 'user'

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
            if user.role == 'admin':
                flash('Please use the Admin Login portal.', 'info')
                return redirect(url_for('admin.login'))
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

@user_bp.route('/dashboard')
@login_required
def dashboard():
    if current_user.role == 'admin':
        return redirect(url_for('admin.dashboard'))
    products = Product.query.all()
    total_sales = sum([p.sales for p in products])
    total_profit = sum([p.profit for p in products])
    total_inventory = sum([p.stock_quantity for p in products])
    low_stock_products = Product.query.filter(Product.stock_quantity < 10).all()

    return render_template('user/dashboard.html',
                           products=products,
                           total_sales=total_sales,
                           total_profit=total_profit,
                           total_inventory=total_inventory,
                           low_stock_count=len(low_stock_products))

@user_bp.route('/upload', methods=['GET', 'POST'])
@login_required
def upload():
    upload_results = None
    causal_results = []
    recommendations = []
    preview_data = []

    if request.method == 'POST':
        if 'file' not in request.files:
            flash('No file part', 'danger')
            return redirect(request.url)

        file = request.files['file']
        if file.filename == '':
            flash('No selected file', 'danger')
            return redirect(request.url)

        if file and file.filename.endswith('.csv'):
            filename = secure_filename(file.filename)
            filepath = os.path.join(current_app.config['UPLOAD_FOLDER'], filename)
            file.save(filepath)

            try:
                df = pd.read_csv(filepath)
                required_cols = ['Product Name', 'Price', 'Marketing Spend', 'Stock Quantity', 'Sales', 'Profit']

                if not all(col in df.columns for col in required_cols):
                    flash(f'CSV must contain: {", ".join(required_cols)}', 'danger')
                    return redirect(request.url)

                total_records = len(df)
                missing_values = df.isnull().sum().sum()
                df_clean = df.drop_duplicates()
                duplicates_removed = total_records - len(df_clean)
                df = df_clean.fillna(0)

                for _, row in df.iterrows():
                    product = Product.query.filter_by(product_name=row['Product Name']).first()
                    if product:
                        product.price = row['Price']
                        product.marketing_spend = row['Marketing Spend']
                        product.stock_quantity = row['Stock Quantity']
                        product.sales = row['Sales']
                        product.profit = row['Profit']
                    else:
                        new_product = Product(
                            product_name=row['Product Name'],
                            price=row['Price'],
                            marketing_spend=row['Marketing Spend'],
                            stock_quantity=row['Stock Quantity'],
                            sales=row['Sales'],
                            profit=row['Profit']
                        )
                        db.session.add(new_product)

                db.session.commit()

                if len(df) >= 5:
                    causal_results = run_causal_analysis(df)

                products = Product.query.all()
                recommendations = generate_recommendations(products)

                upload_results = {
                    'total_records': total_records,
                    'missing_values': missing_values,
                    'duplicates_removed': duplicates_removed,
                    'status': 'Dataset Uploaded and Processed Successfully'
                }

                preview_data = df.head(10).to_dict('records')
                flash('Data analysis complete!', 'success')

            except Exception as e:
                flash(f'Error processing file: {str(e)}', 'danger')
                return redirect(request.url)
        else:
            flash('Only CSV files are allowed.', 'danger')
            return redirect(request.url)

    return render_template('user/upload.html',
                           upload_results=upload_results,
                           causal_results=causal_results,
                           recommendations=recommendations,
                           preview_data=preview_data)

@user_bp.route('/inventory')
@login_required
def inventory():
    products = Product.query.all()
    return render_template('user/inventory.html', products=products)

@user_bp.route('/prediction', methods=['GET', 'POST'])
@login_required
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
def causal_analysis():
    products = Product.query.all()
    if not products:
        flash("No data available for analysis.", "warning")
        return redirect(url_for('user.upload'))

    data = {
        'Price': [p.price for p in products],
        'Marketing Spend': [p.marketing_spend for p in products],
        'Stock Quantity': [p.stock_quantity for p in products],
        'Sales': [p.sales for p in products],
        'Profit': [p.profit for p in products]
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
def recommendations():
    products = Product.query.all()
    recs = generate_recommendations(products)
    return render_template('user/recommendations.html', recommendations=recs)

@user_bp.route('/reports')
@login_required
def reports():
    return render_template('user/reports.html')

@user_bp.route('/download_report')
@login_required
def download_report():
    products = Product.query.all()
    total_sales = sum([p.sales for p in products])
    total_profit = sum([p.profit for p in products])
    pdf_buffer = generate_pdf_report(products, total_sales, total_profit)
    return send_file(pdf_buffer, as_attachment=True, download_name="smartbiz_report.pdf", mimetype='application/pdf')

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
