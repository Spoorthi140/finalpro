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
        except Exception as e:
            app.logger.warning(f"Initial DB setup skipped/failed: {e}")

    return app

if __name__ == '__main__':
    app = create_app()
    app.run(debug=False, port=int(os.environ.get('PORT', 5000)))
