import os
from flask import Blueprint, render_template, redirect, url_for, flash, request, current_app
from flask_login import login_required, current_user
from werkzeug.security import generate_password_hash
from .models import db, User, Product, InventoryLog, AuditLog, Upload, Forecast, Report, InventoryDetection, Prediction
from .auth_utils import admin_required

from flask_login import login_user, logout_user
from werkzeug.security import check_password_hash

admin_bp = Blueprint('admin', __name__, url_prefix='/admin')

@admin_bp.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        if current_user.role == 'Admin':
            return redirect(url_for('admin.dashboard'))
        else:
            logout_user() # log out normal user to prevent session contamination
    if request.method == 'POST':
        email = request.form.get('email', '').strip().lower()
        password = request.form.get('password')
        user = User.query.filter(db.func.lower(User.email) == email).first()
        if user and check_password_hash(user.password, password) and user.role == 'Admin':
            if user.status != 'Active':
                flash('Your account is inactive.', 'warning')
                return redirect(url_for('admin.login'))
            login_user(user)
            db.session.add(AuditLog(user_id=user.id, action="Admin Login", module="Authentication"))
            db.session.commit()
            return redirect(url_for('admin.dashboard'))
        else:
            flash('Invalid Admin Email or Password', 'danger')
    return render_template('admin/login.html')

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

    from datetime import datetime
    def relative_time(dt):
        now = datetime.utcnow()
        diff = now - dt
        if diff.days == 0:
            if diff.seconds < 60:
                return "Just now"
            elif diff.seconds < 3600:
                mins = diff.seconds // 60
                return f"{mins} minute{'s' if mins > 1 else ''} ago"
            else:
                hours = diff.seconds // 3600
                return f"{hours} hour{'s' if hours > 1 else ''} ago"
        elif diff.days == 1:
            return "Yesterday"
        else:
            return f"{diff.days} days ago"

    users = User.query.order_by(User.created_at.desc()).all()
    recent_activities = AuditLog.query.order_by(AuditLog.timestamp.desc()).limit(10).all()
    activities_list = []
    for log in recent_activities:
        activities_list.append({
            'user_name': log.user.full_name if log.user else 'System',
            'action': log.action,
            'relative_time': relative_time(log.timestamp)
        })

    recent_reports = Report.query.order_by(Report.created_at.desc()).limit(10).all()
    recent_detections = InventoryDetection.query.order_by(InventoryDetection.timestamp.desc()).limit(10).all()
    recent_predictions = Prediction.query.order_by(Prediction.timestamp.desc()).limit(10).all()

    return render_template('admin/dashboard.html',
                           stats=stats,
                           users=users,
                           activities=activities_list,
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
    try:
        db.session.delete(user)
        db.session.commit()
        flash(f"User {user.full_name} has been deleted.", "success")
    except Exception as e:
        db.session.rollback()
        flash(f"Error deleting user: {e}", "danger")
    return redirect(url_for('admin.dashboard'))

@admin_bp.route('/user/toggle_status/<int:user_id>', methods=['POST'])
@login_required
@admin_required
def toggle_status(user_id):
    user = db.session.get(User, user_id)
    if not user:
        flash("User not found.", "danger")
        return redirect(url_for('admin.users'))
    try:
        user.is_active = not user.is_active
        db.session.commit()
        status = "activated" if user.is_active else "deactivated"
        flash(f"User {user.full_name} has been {status}.", "info")
    except Exception as e:
        db.session.rollback()
        flash(f"Error updating status: {e}", "danger")
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
        full_name = request.form.get('full_name')
        email = request.form.get('email')
        phone = request.form.get('phone', '0000000000')
        password = request.form.get('password')
        role = request.form.get('role', 'User')

        if User.query.filter_by(email=email).first():
            flash('Email already exists.', 'danger')
            return redirect(url_for('admin.add_user'))

        try:
            new_user = User(
                full_name=full_name,
                email=email,
                phone=phone,
                password=generate_password_hash(password, method='pbkdf2:sha256'),
                role=role
            )
            db.session.add(new_user)
            db.session.commit()
            flash(f'User {full_name} added successfully.', 'success')
            return redirect(url_for('admin.users'))
        except Exception as e:
            db.session.rollback()
            flash(f"Error adding user: {e}", "danger")
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
        try:
            user.full_name = request.form.get('full_name')
            user.email = request.form.get('email')
            user.phone = request.form.get('phone')
            user.role = request.form.get('role')
            user.status = request.form.get('status')
            db.session.commit()
            flash(f'User {user.full_name} updated.', 'success')
            return redirect(url_for('admin.users'))
        except Exception as e:
            db.session.rollback()
            flash(f"Error editing user: {e}", "danger")
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
    try:
        user.password = generate_password_hash(new_pass, method='pbkdf2:sha256')
        db.session.commit()
        flash(f'Password for {user.full_name} has been reset.', 'success')
    except Exception as e:
        db.session.rollback()
        flash(f"Error resetting password: {e}", "danger")
    return redirect(url_for('admin.users'))

@admin_bp.route('/report/delete/<int:report_id>', methods=['POST'])
@login_required
@admin_required
def delete_report(report_id):
    report = db.session.get(Report, report_id)
    if not report:
        flash("Report not found.", "danger")
        return redirect(url_for('admin.reports'))
    try:
        if report.file_path:
            filepath = os.path.join(current_app.config['REPORTS_FOLDER'], report.file_path)
            if os.path.exists(filepath):
                os.remove(filepath)
        db.session.delete(report)
        db.session.commit()
        flash("Report deleted successfully.", "success")
    except Exception as e:
        db.session.rollback()
        flash(f"Error deleting report: {e}", "danger")
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
