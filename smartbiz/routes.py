from flask import Blueprint, render_template, redirect, url_for, flash, request
from flask_login import login_user, logout_user, login_required, current_user
from werkzeug.security import generate_password_hash, check_password_hash
from models import db, User, Product

bp = Blueprint('main', __name__)

@bp.route('/')
def home():
    return render_template('home.html')

@bp.route('/register', methods=['GET', 'POST'])
def register():
    if current_user.is_authenticated:
        return redirect(url_for('main.dashboard'))
    if request.method == 'POST':
        username = request.form.get('username')
        email = request.form.get('email')
        password = request.form.get('password')

        # Security: Don't allow user to set their own role through form
        # First user becomes admin, others are regular users for safety in demo
        user_count = User.query.count()
        role = 'admin' if user_count == 0 else 'user'

        user_exists = User.query.filter_by(email=email).first()
        if user_exists:
            flash('Email already exists.', 'danger')
            return redirect(url_for('main.register'))

        new_user = User(
            username=username,
            email=email,
            password=generate_password_hash(password, method='pbkdf2:sha256'),
            role=role
        )
        db.session.add(new_user)
        db.session.commit()
        flash('Registration successful! Please login.', 'success')
        return redirect(url_for('main.login'))
    return render_template('register.html')

@bp.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('main.dashboard'))
    if request.method == 'POST':
        email = request.form.get('email')
        password = request.form.get('password')
        user = User.query.filter_by(email=email).first()

        if user and check_password_hash(user.password, password):
            login_user(user)
            return redirect(url_for('main.dashboard'))
        else:
            flash('Login failed. Check email and password.', 'danger')
    return render_template('login.html')

@bp.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('main.home'))

@bp.route('/dashboard')
@login_required
def dashboard():
    products = Product.query.all()
    total_sales = sum([p.sales for p in products])
    total_profit = sum([p.profit for p in products])
    total_inventory = sum([p.stock_quantity for p in products])
    low_stock_products = Product.query.filter(Product.stock_quantity < 10).all()

    return render_template('dashboard.html',
                           products=products,
                           total_sales=total_sales,
                           total_profit=total_profit,
                           total_inventory=total_inventory,
                           low_stock_count=len(low_stock_products))

@bp.route('/upload', methods=['GET', 'POST'])
@login_required
def upload():
    if request.method == 'POST':
        if 'file' not in request.files:
            flash('No file part', 'danger')
            return redirect(request.url)

        file = request.files['file']
        if file.filename == '':
            flash('No selected file', 'danger')
            return redirect(request.url)

        if file and file.filename.endswith('.csv'):
            import pandas as pd
            import os
            from flask import current_app
            from werkzeug.utils import secure_filename

            filename = secure_filename(file.filename)
            filepath = os.path.join(current_app.config['UPLOAD_FOLDER'], filename)
            file.save(filepath)

            try:
                df = pd.read_csv(filepath)
                # Validation
                required_cols = ['Product Name', 'Price', 'Marketing Spend', 'Stock Quantity', 'Sales', 'Profit']
                if not all(col in df.columns for col in required_cols):
                    flash(f'CSV must contain: {", ".join(required_cols)}', 'danger')
                    return redirect(request.url)

                # Handling missing values
                df = df.fillna(0)

                # Update Database
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
                flash('Dataset uploaded and processed successfully!', 'success')
                return redirect(url_for('main.dashboard'))

            except Exception as e:
                flash(f'Error processing file: {str(e)}', 'danger')
                return redirect(request.url)
        else:
            flash('Only CSV files are allowed.', 'danger')
            return redirect(request.url)

    return render_template('upload.html')

@bp.route('/inventory')
@login_required
def inventory():
    products = Product.query.all()
    return render_template('inventory.html', products=products)

@bp.route('/prediction', methods=['GET', 'POST'])
@login_required
def prediction():
    prediction_result = None
    if request.method == 'POST':
        from prediction import train_and_predict
        price = float(request.form.get('price', 0))
        marketing = float(request.form.get('marketing', 0))
        stock = float(request.form.get('stock', 0))

        products = Product.query.all()
        prediction_result = train_and_predict(products, price, marketing, stock)

        if prediction_result:
            from models import Prediction
            new_pred = Prediction(
                product_id=products[0].id if products else None, # Simplified for demo
                predicted_sales=prediction_result['predicted_sales'],
                predicted_profit=prediction_result['predicted_profit'],
                stock_out_days=prediction_result['stock_out_days']
            )
            db.session.add(new_pred)
            db.session.commit()
        else:
            flash("Insufficient data to train prediction models. Please upload more products.", "warning")

    return render_template('prediction.html', prediction=prediction_result)

@bp.route('/causal_analysis')
@login_required
def causal_analysis():
    import pandas as pd
    from causal_analysis import run_causal_analysis

    products = Product.query.all()
    if not products:
        flash("No data available for analysis. Please upload a dataset.", "warning")
        return redirect(url_for('main.upload'))

    data = {
        'Price': [p.price for p in products],
        'Marketing Spend': [p.marketing_spend for p in products],
        'Stock Quantity': [p.stock_quantity for p in products],
        'Sales': [p.sales for p in products],
        'Profit': [p.profit for p in products]
    }
    df = pd.DataFrame(data)

    # DoWhy requires more than 1 row to estimate
    if len(df) < 5:
        flash("Need at least 5 products for a reliable causal analysis.", "info")
        causal_results = []
    else:
        causal_results = run_causal_analysis(df)

    return render_template('causal_analysis.html', causal_results=causal_results)

@bp.route('/recommendations')
@login_required
def recommendations():
    from recommendation_engine import generate_recommendations
    products = Product.query.all()
    recs = generate_recommendations(products)
    return render_template('recommendations.html', recommendations=recs)

@bp.route('/download_report')
@login_required
def download_report():
    from report_generator import generate_pdf_report
    from flask import send_file

    products = Product.query.all()
    total_sales = sum([p.sales for p in products])
    total_profit = sum([p.profit for p in products])

    pdf_buffer = generate_pdf_report(products, total_sales, total_profit)

    return send_file(
        pdf_buffer,
        as_attachment=True,
        download_name="smartbiz_report.pdf",
        mimetype='application/pdf'
    )

@bp.route('/process_inventory', methods=['POST'])
@login_required
def process_inventory():
    from inventory_monitor import detect_objects
    data = request.get_json()
    image_data = data.get('image')
    count, processed_image = detect_objects(image_data)
    return {'count': count, 'processed_image': processed_image}

@bp.route('/update_inventory', methods=['POST'])
@login_required
def update_inventory():
    from models import InventoryLog
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

@bp.route('/reports')
@login_required
def reports():
    return render_template('reports.html')

@bp.route('/admin')
@login_required
def admin():
    if current_user.role != 'admin':
        flash('Access denied.', 'danger')
        return redirect(url_for('main.dashboard'))
    users = User.query.all()
    return render_template('admin.html', users=users)
