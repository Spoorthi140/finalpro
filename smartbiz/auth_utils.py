from functools import wraps
from flask import abort, flash, redirect, url_for
from flask_login import current_user

def admin_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if not current_user.is_authenticated or current_user.role not in ['Super Admin', 'Business Admin']:
            flash("You do not have permission to access this page.", "danger")
            return redirect(url_for('user.login'))
        return f(*args, **kwargs)
    return decorated_function

def role_required(roles):
    def decorator(f):
        @wraps(f)
        def decorated_function(*args, **kwargs):
            if not current_user.is_authenticated or current_user.role not in roles:
                flash("You do not have permission to access this page.", "danger")
                return redirect(url_for('user.dashboard'))
            return f(*args, **kwargs)
        return decorated_function
    return decorator
