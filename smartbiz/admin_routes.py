import os
from flask import Blueprint, render_template, redirect, url_for, flash, request, current_app
from flask_login import login_required, current_user
from werkzeug.security import generate_password_hash
from .models import db, User, Product, InventoryLog, AuditLog, Upload, Forecast, Report, InventoryDetection, Prediction
from .auth_utils import admin_required

admin_bp = Blueprint('admin', __name__, url_prefix='/admin')

@admin_bp.route('/login')
def login():
    return redirect(url_for('user.login'))

@admin_bp.route('/dashboard')
@login_required
@admin_required
def dashboard():
    stats = {
        'total_users': User.query.count(),
        'total_products': Product.query.count(),
        'total_reports': Report.query.count(),
        'total_detections': InventoryDetection.query.count(),
        'total_uploads': Upload.query.count()
    }

    users = User.query.order_by(User.created_at.desc()).all()
    recent_activities = AuditLog.query.order_by(AuditLog.timestamp.desc()).limit(10).all()
    recent_reports = Report.query.order_by(Report.created_at.desc()).limit(10).all()
    recent_detections = InventoryDetection.query.order_by(InventoryDetection.timestamp.desc()).limit(10).all()
    recent_predictions = Prediction.query.order_by(Prediction.timestamp.desc()).limit(10).all()

    return render_template('admin/dashboard.html',
                           stats=stats,
                           users=users,
                           activities=recent_activities,
                           reports=recent_reports,
                           detections=recent_detections,
                           predictions=recent_predictions)

@admin_bp.route('/user/delete/<int:user_id>', methods=['POST'])
@login_required
@admin_required
def delete_user(user_id):
    if user_id == current_user.id:
        flash("You cannot delete your own admin account.", "danger")
        return redirect(url_for('admin.dashboard'))

    user = db.session.get(User, user_id)
    if not user:
        flash("User not found.", "danger")
        return redirect(url_for('admin.dashboard'))
    db.session.delete(user)
    db.session.commit()
    flash(f"User {user.name} has been deleted.", "success")
    return redirect(url_for('admin.dashboard'))

@admin_bp.route('/user/toggle_status/<int:user_id>', methods=['POST'])
@login_required
@admin_required
def toggle_status(user_id):
    user = db.session.get(User, user_id)
    if not user:
        flash("User not found.", "danger")
        return redirect(url_for('admin.users'))
    user.is_active = not user.is_active
    db.session.commit()
    status = "activated" if user.is_active else "deactivated"
    flash(f"User {user.name} has been {status}.", "info")
    return redirect(url_for('admin.users'))

@admin_bp.route('/users')
@login_required
@admin_required
def users():
    users_list = User.query.all()
    return render_template('admin/users.html', users=users_list)

@admin_bp.route('/user/add', methods=['GET', 'POST'])
@login_required
@admin_required
def add_user():
    if request.method == 'POST':
        name = request.form.get('name')
        email = request.form.get('email')
        password = request.form.get('password')
        role = request.form.get('role', 'User')

        if User.query.filter_by(email=email).first():
            flash('Email already exists.', 'danger')
            return redirect(url_for('admin.add_user'))

        new_user = User(
            name=name,
            email=email,
            password=generate_password_hash(password, method='pbkdf2:sha256'),
            role=role
        )
        db.session.add(new_user)
        db.session.commit()
        flash(f'User {name} added successfully.', 'success')
        return redirect(url_for('admin.users'))
    return render_template('admin/add_user.html')

@admin_bp.route('/user/edit/<int:user_id>', methods=['GET', 'POST'])
@login_required
@admin_required
def edit_user(user_id):
    user = db.session.get(User, user_id)
    if not user:
        flash("User not found.", "danger")
        return redirect(url_for('admin.users'))
    if request.method == 'POST':
        user.name = request.form.get('name')
        user.email = request.form.get('email')
        user.role = request.form.get('role')
        user.status = request.form.get('status')
        db.session.commit()
        flash(f'User {user.name} updated.', 'success')
        return redirect(url_for('admin.users'))
    return render_template('admin/edit_user.html', user=user)

@admin_bp.route('/user/reset_password/<int:user_id>', methods=['POST'])
@login_required
@admin_required
def reset_password(user_id):
    user = db.session.get(User, user_id)
    if not user:
        flash("User not found.", "danger")
        return redirect(url_for('admin.users'))
    new_pass = request.form.get('new_password')
    if not new_pass:
        flash('Password cannot be empty.', 'danger')
        return redirect(url_for('admin.users'))
    user.password = generate_password_hash(new_pass, method='pbkdf2:sha256')
    db.session.commit()
    flash(f'Password for {user.name} has been reset.', 'success')
    return redirect(url_for('admin.users'))

@admin_bp.route('/report/delete/<int:report_id>', methods=['POST'])
@login_required
@admin_required
def delete_report(report_id):
    report = db.session.get(Report, report_id)
    if not report:
        flash("Report not found.", "danger")
        return redirect(url_for('admin.dashboard'))
    if report.file_path:
        filepath = os.path.join(current_app.config['REPORTS_FOLDER'], report.file_path)
        if os.path.exists(filepath):
            os.remove(filepath)
    db.session.delete(report)
    db.session.commit()
    flash("Report deleted successfully.", "success")
    return redirect(url_for('admin.reports'))

@admin_bp.route('/inventory')
@login_required
@admin_required
def inventory():
    products = Product.query.all()
    return render_template('admin/inventory.html', products=products)

@admin_bp.route('/predictions')
@login_required
@admin_required
def predictions():
    preds = Prediction.query.order_by(Prediction.timestamp.desc()).all()
    return render_template('admin/predictions.html', predictions=preds)

@admin_bp.route('/recommendations')
@login_required
@admin_required
def recommendations():
    from .models import Recommendation
    recs = Recommendation.query.order_by(Recommendation.created_at.desc()).all()
    return render_template('admin/recommendations.html', recommendations=recs)

@admin_bp.route('/reports')
@login_required
@admin_required
def reports():
    reps = Report.query.order_by(Report.created_at.desc()).all()
    return render_template('admin/reports.html', reports=reps)

@admin_bp.route('/analytics')
@login_required
@admin_required
def analytics():
    # Basic analytics overview for admin
    user_count = User.query.count()
    product_count = Product.query.count()
    report_count = Report.query.count()
    return render_template('admin/analytics.html', users=user_count, products=product_count, reports=report_count)

@admin_bp.route('/settings', methods=['GET', 'POST'])
@login_required
@admin_required
def settings():
    if request.method == 'POST':
        # Logic for system settings can be added here
        flash('System settings updated.', 'success')
        return redirect(url_for('admin.settings'))
    return render_template('admin/settings.html')
