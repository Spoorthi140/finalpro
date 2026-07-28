import os
from datetime import timedelta
from flask import Flask, render_template
from flask_login import LoginManager
from flask_wtf.csrf import CSRFProtect
from .models import db, User
from .user_routes import user_bp
from .admin_routes import admin_bp
from werkzeug.security import generate_password_hash

csrf = CSRFProtect()

def create_app(config=None):
    app = Flask(__name__)

    # Security: Use environment variable with fallback
    app.config['SECRET_KEY'] = os.environ.get('SECRET_KEY', 'smartbiz-enterprise-secure-key-2024')

    # Session security parameters
    app.config['PERMANENT_SESSION_LIFETIME'] = timedelta(minutes=30)
    app.config['SESSION_COOKIE_HTTPONLY'] = True
    app.config['SESSION_COOKIE_SAMESITE'] = 'Lax'
    # Use SECURE cookie only if in production/HTTPS, but support override
    app.config['SESSION_COOKIE_SECURE'] = os.environ.get('SESSION_COOKIE_SECURE', 'False').lower() == 'true'
    # Max file upload size limit (16MB)
    app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024

    # Database Configuration
    default_db = 'mysql+pymysql://root:password@localhost/smartbiz_db'
    db_uri = os.environ.get('DATABASE_URL')
    if not db_uri or db_uri == 'sqlite:///:memory:':
        if os.environ.get('USE_MYSQL'):
            db_uri = default_db
        else:
            db_uri = 'sqlite:///smartbiz.db'
    app.config['SQLALCHEMY_DATABASE_URI'] = db_uri
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

    # Uploads Configuration
    app.config['UPLOAD_FOLDER'] = os.path.join(app.root_path, 'static/uploads')
    app.config['REPORTS_FOLDER'] = os.path.join(app.root_path, 'static/reports')
    app.config['DETECTIONS_FOLDER'] = os.path.join(app.root_path, 'static/detections')
    app.config['SAMPLE_IMAGES_FOLDER'] = os.path.join(app.root_path, 'static/sample_images')

    for folder in [app.config['UPLOAD_FOLDER'], app.config['REPORTS_FOLDER'],
                   app.config['DETECTIONS_FOLDER'], app.config['SAMPLE_IMAGES_FOLDER']]:
        if not os.path.exists(folder):
            os.makedirs(folder)

    if config:
        app.config.update(config)

    db.init_app(app)
    csrf.init_app(app)

    login_manager = LoginManager()
    login_manager.login_view = 'user.login'
    login_manager.init_app(app)

    @login_manager.user_loader
    def load_user(user_id):
        return db.session.get(User, int(user_id))

    app.register_blueprint(user_bp)
    app.register_blueprint(admin_bp)

    @app.errorhandler(403)
    def forbidden(e):
        return render_template('403.html'), 403

    @app.errorhandler(404)
    def page_not_found(e):
        return render_template('404.html'), 404

    @app.errorhandler(500)
    def internal_server_error(e):
        return render_template('500.html'), 500

    with app.app_context():
        try:
            db.create_all()
            # Auto-initialize default Admin if not exists
            admin_email = os.environ.get('ADMIN_EMAIL', 'admin@smartbiz.com')
            if not User.query.filter_by(email=admin_email).first():
                admin = User(
                    full_name='SystemAdmin',
                    email=admin_email,
                    phone='9876543210',
                    password=generate_password_hash(os.environ.get('ADMIN_PASSWORD', 'Admin@123'), method='pbkdf2:sha256'),
                    role='Admin'
                )
                db.session.add(admin)
                db.session.commit()

            # Auto-initialize Product database from sample_data.csv if empty
            from .models import Product
            if Product.query.first() is None:
                import csv
                from datetime import datetime
                csv_path = os.path.join(app.root_path, 'datasets', 'sample_data.csv')

                # Overwrite/generate 150-row high fidelity dataset
                templates = [
                    ("Smart LED TV", "Electronics", 45000, 15),
                    ("Wireless Earbuds", "Electronics", 3000, 50),
                    ("Bluetooth Speaker", "Electronics", 2500, 40),
                    ("Laptop Stand", "Electronics", 1500, 30),
                    ("Coffee Maker", "Electronics", 6000, 20),
                    ("Organic Green Tea", "Beverages", 250, 100),
                    ("Diet Cola Can", "Beverages", 40, 200),
                    ("Sparkling Water", "Beverages", 80, 150),
                    ("Fruit Juice Pack", "Beverages", 120, 120),
                    ("Energy Drink", "Beverages", 150, 80),
                    ("Premium Olive Oil", "Groceries", 800, 60),
                    ("Basmati Rice 5kg", "Groceries", 650, 80),
                    ("Whole Wheat Flour", "Groceries", 280, 100),
                    ("Organic Honey", "Groceries", 350, 50),
                    ("Mixed Almonds", "Groceries", 450, 90),
                    ("Denim Jacket", "Apparel", 2500, 30),
                    ("Cotton T-Shirt", "Apparel", 600, 120),
                    ("Running Shoes", "Apparel", 3500, 40),
                    ("Leather Wallet", "Apparel", 1200, 70),
                    ("Woolen Socks", "Apparel", 300, 150),
                    ("Ergonomic Chair", "Furniture", 12000, 15),
                    ("Wooden Coffee Table", "Furniture", 8500, 10),
                    ("Desk Organizer", "Furniture", 800, 50),
                    ("Table Lamp", "Furniture", 1500, 40),
                    ("Bean Bag", "Furniture", 2200, 25),
                    ("Smartphone", "Electronics", 25000, 20),
                    ("Fitness Tracker", "Electronics", 4000, 60),
                    ("Ceramic Mug", "Groceries", 300, 110),
                    ("Chocolate Bar", "Beverages", 120, 180),
                    ("Lounge Sofa", "Furniture", 35000, 5),
                    ("Yoga Mat", "Apparel", 1000, 60),
                    ("Backpack", "Apparel", 1800, 80),
                    ("Sunglasses", "Apparel", 1500, 50),
                    ("Washing Machine", "Electronics", 22000, 8),
                    ("Vacuum Cleaner", "Electronics", 11000, 12)
                ]

                # Ensure datasets folder exists
                datasets_dir = os.path.dirname(csv_path)
                if not os.path.exists(datasets_dir):
                    os.makedirs(datasets_dir)

                # Generate exactly 150 rows
                rows = []
                for idx in range(1, 151):
                    tpl = templates[(idx - 1) % len(templates)]
                    p_name, cat, price, base_stock = tpl

                    year = 2022 + ((idx - 1) // 30)
                    month = 1 + (((idx - 1) % 30) % 12)
                    day = 1 + (((idx - 1) % 30) % 28)
                    date_str = f"{year}-{month:02d}-{day:02d}"

                    sales = 10 + (idx % 40) + ((idx // 30) * 8)
                    marketing_spend = 500 + (idx % 10) * 100 + ((idx // 30) * 150)
                    stock_qty = base_stock + (idx % 20)
                    revenue = sales * price
                    profit = round(revenue * 0.35 - (marketing_spend * 0.05), 2)

                    rows.append({
                        'Product Name': p_name,
                        'Category': cat,
                        'Price': price,
                        'Marketing Spend': marketing_spend,
                        'Stock Quantity': stock_qty,
                        'Sales': sales,
                        'Revenue': revenue,
                        'Profit': profit,
                        'Date': date_str
                    })

                with open(csv_path, mode='w', encoding='utf-8', newline='') as f:
                    writer = csv.DictWriter(f, fieldnames=['Product Name', 'Category', 'Price', 'Marketing Spend', 'Stock Quantity', 'Sales', 'Revenue', 'Profit', 'Date'])
                    writer.writeheader()
                    writer.writerows(rows)

                # Now seed database from the generated file
                for idx, row in enumerate(rows, start=1):
                    p_name = row['Product Name']
                    cat = row['Category']
                    price = float(row['Price'])
                    marketing_spend = float(row['Marketing Spend'])
                    stock_quantity = int(row['Stock Quantity'])
                    sales = int(row['Sales'])
                    revenue = float(row['Revenue'])
                    profit = float(row['Profit'])
                    dt = datetime.strptime(row['Date'], '%Y-%m-%d')

                    regions = ['North', 'South', 'East', 'West']
                    warehouses = ['Main Warehouse', 'Secondary Warehouse']

                    p = Product(
                        product_id_str=f"PROD-{idx:04d}",
                        product_name=p_name,
                        category=cat,
                        price=price,
                        cost_price=round(price * 0.7, 2),
                        selling_price=price,
                        marketing_spend=marketing_spend,
                        discount=10.0,
                        stock_quantity=stock_quantity,
                        sales=sales,
                        units_sold=sales,
                        revenue=revenue,
                        profit=profit,
                        customer_rating=4.5,
                        customer_satisfaction=88.0,
                        returns=2,
                        region=regions[idx % len(regions)],
                        warehouse=warehouses[idx % len(warehouses)],
                        season='All Season',
                        minimum_threshold=15,
                        maximum_threshold=1000,
                        supplier='Alpha Distributor',
                        description=f"Premium grade {p_name} supplied by Alpha Distributor.",
                        date=dt,
                        created_at=dt,
                        last_updated=dt
                    )
                    db.session.add(p)
                db.session.commit()
                app.logger.info("Successfully generated 150-row premium dataset and seeded Product database!")
        except Exception as e:
            app.logger.warning(f"Initial DB setup skipped/failed: {e}")

    return app

if __name__ == '__main__':
    app = create_app()
    app.run(debug=False, port=int(os.environ.get('PORT', 5000)))
