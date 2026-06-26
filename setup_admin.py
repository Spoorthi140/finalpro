from smartbiz.app import create_app
from smartbiz.models import db, User
from werkzeug.security import generate_password_hash

def setup():
    app = create_app()
    with app.app_context():
        # First check if Super Admin exists
        admin = User.query.filter_by(email='admin@smartbiz.com').first()
        if not admin:
            admin = User(
                username='SuperAdmin',
                email='admin@smartbiz.com',
                password=generate_password_hash('admin123', method='pbkdf2:sha256'),
                role='Super Admin'
            )
            db.session.add(admin)
            db.session.commit()
            print("Super Admin created: admin@smartbiz.com / admin123")
        else:
            # Ensure password is reset to known one for the user
            admin.password = generate_password_hash('admin123', method='pbkdf2:sha256')
            admin.role = 'Super Admin'
            db.session.commit()
            print("Super Admin credentials updated: admin@smartbiz.com / admin123")

if __name__ == "__main__":
    setup()
