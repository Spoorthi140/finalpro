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
    app.config['SQLALCHEMY_DATABASE_URI'] = os.environ.get('DATABASE_URL', default_db if os.environ.get('USE_MYSQL') else 'sqlite:///smartbiz.db')
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
        # Auto-create tables for SQLite; for MySQL, user usually runs a script or we attempt creation
        try:
            db.create_all()
        except Exception as e:
            # Skip if DB is not reachable yet
            app.logger.warning(f"Initial DB connection/table creation skipped: {e}")

    return app

if __name__ == '__main__':
    app = create_app()
    app.run(debug=True, port=int(os.environ.get('PORT', 5000)))
