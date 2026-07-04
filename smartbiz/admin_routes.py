import os
from flask import Blueprint, render_template, redirect, url_for, flash, request, current_app
from flask_login import login_required, current_user
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

    user = User.query.get_or_404(user_id)
    db.session.delete(user)
    db.session.commit()
    flash(f"User {user.username} has been deleted.", "success")
    return redirect(url_for('admin.dashboard'))

@admin_bp.route('/user/toggle_status/<int:user_id>', methods=['POST'])
@login_required
@admin_required
def toggle_status(user_id):
    user = User.query.get_or_404(user_id)
    user.is_active = not user.is_active
    db.session.commit()
    status = "activated" if user.is_active else "deactivated"
    flash(f"User {user.username} has been {status}.", "info")
    return redirect(url_for('admin.dashboard'))

@admin_bp.route('/report/delete/<int:report_id>', methods=['POST'])
@login_required
@admin_required
def delete_report(report_id):
    report = Report.query.get_or_404(report_id)
    if report.file_path:
        filepath = os.path.join(current_app.config['REPORTS_FOLDER'], report.file_path)
        if os.path.exists(filepath):
            os.remove(filepath)
    db.session.delete(report)
    db.session.commit()
    flash("Report deleted successfully.", "success")
    return redirect(url_for('admin.dashboard'))
