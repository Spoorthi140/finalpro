from flask import Blueprint, render_template, redirect, url_for, flash, request
from flask_login import login_user, logout_user, login_required, current_user
from werkzeug.security import check_password_hash
from models import db, User, Product, Prediction

admin_bp = Blueprint('admin', __name__, url_prefix='/admin')

@admin_bp.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        if current_user.role == 'admin':
            return redirect(url_for('admin.dashboard'))
        return redirect(url_for('user.dashboard'))

    if request.method == 'POST':
        email = request.form.get('email')
        password = request.form.get('password')
        user = User.query.filter_by(email=email).first()

        if user and check_password_hash(user.password, password):
            if user.role != 'admin':
                flash('Access denied. This portal is for admins only.', 'danger')
                return redirect(url_for('admin.login'))
            login_user(user)
            return redirect(url_for('admin.dashboard'))
        else:
            flash('Admin login failed.', 'danger')
    return render_template('admin/login.html')

@admin_bp.route('/dashboard')
@login_required
def dashboard():
    if current_user.role != 'admin':
        flash('Access denied.', 'danger')
        return redirect(url_for('user.dashboard'))

    users = User.query.all()
    products = Product.query.all()
    predictions = Prediction.query.order_by(Prediction.timestamp.desc()).limit(10).all()

    return render_template('admin/dashboard.html',
                           users=users,
                           products=products,
                           predictions=predictions)

@admin_bp.route('/user/delete/<int:user_id>', methods=['POST'])
@login_required
def delete_user(user_id):
    if current_user.role != 'admin':
        return {"error": "Unauthorized"}, 403

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
def delete_product(product_id):
    if current_user.role != 'admin':
        return {"error": "Unauthorized"}, 403

    product = Product.query.get_or_404(product_id)
    db.session.delete(product)
    db.session.commit()
    flash(f"Product {product.product_name} deleted.", "success")
    return redirect(url_for('admin.dashboard'))
