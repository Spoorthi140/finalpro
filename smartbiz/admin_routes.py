from flask import Blueprint, render_template, redirect, url_for, flash, request
from flask_login import login_user, logout_user, login_required, current_user
from werkzeug.security import check_password_hash, generate_password_hash
from .models import db, User, Product, Prediction, InventoryLog, AuditLog, Upload
from .auth_utils import admin_required

admin_bp = Blueprint('admin', __name__, url_prefix='/admin')

@admin_bp.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        if current_user.role in ['Super Admin', 'Business Admin']:
            return redirect(url_for('admin.dashboard'))
        return redirect(url_for('user.dashboard'))

    if request.method == 'POST':
        email = request.form.get('email')
        password = request.form.get('password')
        user = User.query.filter_by(email=email).first()

        if user and check_password_hash(user.password, password):
            if user.role not in ['Super Admin', 'Business Admin']:
                flash('Access denied. This portal is for admins only.', 'danger')
                return redirect(url_for('admin.login'))
            login_user(user)
            return redirect(url_for('admin.dashboard'))
        else:
            flash('Admin login failed.', 'danger')
    return render_template('admin/login.html')

@admin_bp.route('/dashboard')
@login_required
@admin_required
def dashboard():
    users = User.query.all()
    products = Product.query.all()
    inventory_logs = InventoryLog.query.order_by(InventoryLog.timestamp.desc()).limit(10).all()
    uploads = Upload.query.order_by(Upload.upload_date.desc()).limit(10).all()

    return render_template('admin/dashboard.html',
                           users=users,
                           products=products,
                           inventory_logs=inventory_logs,
                           uploads=uploads)

@admin_bp.route('/user/create', methods=['POST'])
@login_required
@admin_required
def create_user():
    username = request.form.get('username')
    email = request.form.get('email')
    password = request.form.get('password')
    role = request.form.get('role')

    user_exists = User.query.filter_by(email=email).first()
    if user_exists:
        flash('Email already exists.', 'danger')
        return redirect(url_for('admin.dashboard'))

    new_user = User(
        username=username,
        email=email,
        password=generate_password_hash(password, method='pbkdf2:sha256'),
        role=role
    )
    db.session.add(new_user)
    db.session.commit()
    flash(f'User {username} created successfully.', 'success')
    return redirect(url_for('admin.dashboard'))

@admin_bp.route('/user/update/<int:user_id>', methods=['POST'])
@login_required
@admin_required
def update_user(user_id):
    user = User.query.get_or_404(user_id)
    user.role = request.form.get('role')
    user.is_active = 'is_active' in request.form
    db.session.commit()
    flash(f'User {user.username} updated.', 'success')
    return redirect(url_for('admin.dashboard'))

@admin_bp.route('/user/delete/<int:user_id>', methods=['POST'])
@login_required
@admin_required
def delete_user(user_id):

    user = User.query.get_or_404(user_id)
    if user.id == current_user.id:
        flash("You cannot delete yourself.", "danger")
    else:
        db.session.delete(user)
        db.session.commit()
        flash(f"User {user.username} deleted.", "success")
    return redirect(url_for('admin.dashboard'))

@admin_bp.route('/product/delete/<int:product_id>', methods=['POST'])
@login_required
@admin_required
def delete_product(product_id):

    product = Product.query.get_or_404(product_id)
    db.session.delete(product)
    db.session.commit()
    flash(f"Product {product.product_name} deleted.", "success")
    return redirect(url_for('admin.dashboard'))
