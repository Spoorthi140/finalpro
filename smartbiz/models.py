from flask_sqlalchemy import SQLAlchemy
from flask_login import UserMixin
from datetime import datetime

db = SQLAlchemy()

class Role(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(50), unique=True, nullable=False) # Super Admin, Business Admin, Manager, Analyst, Viewer
    description = db.Column(db.String(200))

class User(UserMixin, db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(150), unique=True, nullable=False)
    email = db.Column(db.String(150), unique=True, nullable=False)
    password = db.Column(db.String(256), nullable=False)
    role = db.Column(db.String(20), default='Viewer')
    is_active = db.Column(db.Boolean, default=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

class Company(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(200), nullable=False)
    industry = db.Column(db.String(100))
    business_type = db.Column(db.String(100))
    address = db.Column(db.Text)
    contact_number = db.Column(db.String(20))
    branch_count = db.Column(db.Integer, default=1)

class Branch(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    company_id = db.Column(db.Integer, db.ForeignKey('company.id'), nullable=False)
    name = db.Column(db.String(200), nullable=False)
    location = db.Column(db.String(200))

class Upload(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    filename = db.Column(db.String(200), nullable=False)
    file_type = db.Column(db.String(10)) # CSV or XLSX
    upload_date = db.Column(db.DateTime, default=datetime.utcnow)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))
    status = db.Column(db.String(50)) # Validated, Error
    quality_score = db.Column(db.Float)

class Product(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    product_name = db.Column(db.String(100), nullable=False)
    category = db.Column(db.String(100))
    price = db.Column(db.Float, nullable=False)
    marketing_spend = db.Column(db.Float, default=0.0)
    stock_quantity = db.Column(db.Integer, default=0)
    sales = db.Column(db.Integer, default=0)
    revenue = db.Column(db.Float, default=0.0)
    profit = db.Column(db.Float, default=0.0)
    date = db.Column(db.DateTime, default=datetime.utcnow)
    last_updated = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

class Sale(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    product_id = db.Column(db.Integer, db.ForeignKey('product.id'), nullable=False)
    quantity = db.Column(db.Integer, nullable=False)
    amount = db.Column(db.Float, nullable=False)
    date = db.Column(db.DateTime, default=datetime.utcnow)

class Inventory(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    product_id = db.Column(db.Integer, db.ForeignKey('product.id'), nullable=False)
    current_stock = db.Column(db.Integer, default=0)
    min_stock_level = db.Column(db.Integer, default=10)
    last_restocked = db.Column(db.DateTime)

class InventoryLog(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    product_id = db.Column(db.Integer, db.ForeignKey('product.id'), nullable=False)
    detected_quantity = db.Column(db.Integer)
    image_path = db.Column(db.String(255))
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)

class Prediction(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    product_id = db.Column(db.Integer, db.ForeignKey('product.id'), nullable=False)
    predicted_sales = db.Column(db.Float)
    predicted_profit = db.Column(db.Float)
    stock_out_days = db.Column(db.Float)
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)

class Forecast(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    target_metric = db.Column(db.String(50)) # Sales, Revenue, Profit
    forecast_date = db.Column(db.DateTime)
    forecast_value = db.Column(db.Float)
    model_used = db.Column(db.String(50)) # XGBoost, ARIMA, Prophet
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

class Recommendation(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    product_id = db.Column(db.Integer, db.ForeignKey('product.id'))
    category = db.Column(db.String(50)) # Pricing, Marketing, Inventory
    message = db.Column(db.Text)
    priority = db.Column(db.String(20)) # High, Medium, Low
    action = db.Column(db.String(100))
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

class Report(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    report_name = db.Column(db.String(200))
    report_type = db.Column(db.String(50)) # Sales, Inventory, Executive
    file_path = db.Column(db.String(255))
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    created_by = db.Column(db.Integer, db.ForeignKey('user.id'))

class CustomerSegment(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    segment_name = db.Column(db.String(100)) # Loyal, At Risk, New, VIP
    customer_count = db.Column(db.Integer)
    avg_clv = db.Column(db.Float) # Average Customer Lifetime Value
    analysis_date = db.Column(db.DateTime, default=datetime.utcnow)

class BusinessMetric(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    metric_name = db.Column(db.String(100)) # Business Health Score, Revenue Risk
    metric_value = db.Column(db.Float)
    category = db.Column(db.String(50))
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)

class AuditLog(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))
    action = db.Column(db.String(200))
    module = db.Column(db.String(100))
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)
    ip_address = db.Column(db.String(45))
