import os
from flask import Flask
from flask_login import LoginManager
from .models import db, User
from .user_routes import user_bp
from .admin_routes import admin_bp

def create_app(config=None):
    app = Flask(__name__)
    app.config['SECRET_KEY'] = os.environ.get('SECRET_KEY', 'dev-key-for-smartbiz')
    # Default to MySQL as requested, with SQLite as local fallback
    default_db = 'mysql+pymysql://root:password@localhost/smartbiz_db'
    db_uri = os.environ.get('DATABASE_URL')

    # Force persistent SQLite if environment default is :memory: unless we are in a test config
    if not db_uri or db_uri == 'sqlite:///:memory:':
        if os.environ.get('USE_MYSQL'):
            db_uri = default_db
        else:
            db_uri = 'sqlite:///smartbiz.db'

    app.config['SQLALCHEMY_DATABASE_URI'] = db_uri
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
    app.config['UPLOAD_FOLDER'] = os.path.join(os.path.abspath(os.path.dirname(__file__)), 'static/uploads')

    if config:
        app.config.update(config)

    if not os.path.exists(app.config['UPLOAD_FOLDER']):
        os.makedirs(app.config['UPLOAD_FOLDER'])

    db.init_app(app)

    login_manager = LoginManager()
    login_manager.login_view = 'user.login'
    login_manager.init_app(app)

    @login_manager.user_loader
    def load_user(user_id):
        return User.query.get(int(user_id))

    app.register_blueprint(user_bp)
    app.register_blueprint(admin_bp)

    with app.app_context():
        # Auto-create tables
        try:
            db.create_all()
            # Ensure at least one Super Admin exists
            from .models import User
            from werkzeug.security import generate_password_hash
            if not User.query.filter_by(email='admin@smartbiz.com').first():
                admin = User(
                    username='Admin',
                    email='admin@smartbiz.com',
                    password=generate_password_hash('admin123', method='pbkdf2:sha256'),
                    role='Super Admin'
                )
                db.session.add(admin)
                db.session.commit()
        except Exception as e:
            app.logger.warning(f"Initial DB setup skipped: {e}")

    return app

if __name__ == '__main__':
    app = create_app()
    app.run(debug=True, port=int(os.environ.get('PORT', 5000)))
