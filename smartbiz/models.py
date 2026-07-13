from flask_sqlalchemy import SQLAlchemy
from flask_login import UserMixin
from datetime import datetime

db = SQLAlchemy()

class User(UserMixin, db.Model):
    id = db.Column(db.Integer, primary_key=True)
    full_name = db.Column(db.String(150), nullable=False)
    email = db.Column(db.String(150), unique=True, nullable=False)
    phone = db.Column(db.String(20), nullable=True)
    password = db.Column(db.String(256), nullable=False)
    role = db.Column(db.String(20), default='User') # 'Admin' or 'User'
    status = db.Column(db.String(20), default='Active') # 'Active' or 'Inactive'
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    @property
    def is_active(self):
        return self.status == 'Active'

    @is_active.setter
    def is_active(self, value):
        self.status = 'Active' if value else 'Inactive'

    uploads = db.relationship('Upload', backref='uploader', lazy=True, cascade="all, delete-orphan")
    reports = db.relationship('Report', backref='author', lazy=True, cascade="all, delete-orphan")
    audit_logs = db.relationship('AuditLog', backref='user', lazy=True, cascade="all, delete-orphan")

class Company(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(200), nullable=False)
    industry = db.Column(db.String(100))
    business_type = db.Column(db.String(100))
    address = db.Column(db.Text)
    contact_number = db.Column(db.String(20))
    branch_count = db.Column(db.Integer, default=1)

class Upload(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    filename = db.Column(db.String(200), nullable=False)
    file_type = db.Column(db.String(10)) # CSV, XLSX, IMG
    upload_date = db.Column(db.DateTime, default=datetime.utcnow)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))
    status = db.Column(db.String(50))
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
    minimum_threshold = db.Column(db.Integer, default=10)
    maximum_threshold = db.Column(db.Integer, default=1000)
    supplier = db.Column(db.String(150))
    description = db.Column(db.Text)
    image_path = db.Column(db.String(255))
    date = db.Column(db.DateTime, default=datetime.utcnow)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    last_updated = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    inventory_logs = db.relationship('InventoryLog', backref='product', lazy=True, cascade="all, delete-orphan")
    predictions = db.relationship('Prediction', backref='product', lazy=True, cascade="all, delete-orphan")

class InventoryLog(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    product_id = db.Column(db.Integer, db.ForeignKey('product.id'), nullable=False)
    detected_quantity = db.Column(db.Integer)
    difference = db.Column(db.Integer)
    status = db.Column(db.String(50))
    image_path = db.Column(db.String(255))
    confidence_score = db.Column(db.Float)
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)

class InventoryDetection(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    image_name = db.Column(db.String(255))
    original_path = db.Column(db.String(255))
    annotated_path = db.Column(db.String(255))
    box_count = db.Column(db.Integer)
    average_confidence = db.Column(db.Float)
    processing_time = db.Column(db.Float) # in ms
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)

    user = db.relationship('User', backref=db.backref('detections', lazy=True))

class Prediction(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    product_id = db.Column(db.Integer, db.ForeignKey('product.id'), nullable=False)
    predicted_sales = db.Column(db.Float)
    predicted_profit = db.Column(db.Float)
    stock_out_days = db.Column(db.Float)
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)

class Forecast(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    target_metric = db.Column(db.String(50))
    forecast_date = db.Column(db.DateTime)
    forecast_value = db.Column(db.Float)
    model_used = db.Column(db.String(50))
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

class Recommendation(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    product_id = db.Column(db.Integer, db.ForeignKey('product.id'))
    category = db.Column(db.String(50))
    message = db.Column(db.Text)
    priority = db.Column(db.String(20))
    action = db.Column(db.String(100))
    expected_impact = db.Column(db.String(100))
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

class Report(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    report_name = db.Column(db.String(200))
    report_type = db.Column(db.String(50)) # Sales, Inventory, Executive, AI Detection
    file_path = db.Column(db.String(255))
    file_size = db.Column(db.String(20))
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))

class AuditLog(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'))
    action = db.Column(db.String(200))
    module = db.Column(db.String(100))
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)
    ip_address = db.Column(db.String(45))
