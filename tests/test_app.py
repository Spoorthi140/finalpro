import pytest
from smartbiz.app import create_app
from smartbiz.models import db, User

@pytest.fixture
def app():
    app = create_app({'TESTING': True, 'SQLALCHEMY_DATABASE_URI': 'sqlite:///:memory:'})
    with app.app_context():
        db.create_all()
        yield app
        db.drop_all()

@pytest.fixture
def client(app):
    return app.test_client()

def test_home_page(client):
    rv = client.get('/')
    assert rv.status_code == 200
    assert b"SmartBiz Enterprise" in rv.data

def test_login_page(client):
    rv = client.get('/login')
    assert rv.status_code == 200
    assert b"Login" in rv.data

def test_admin_login_page(client):
    rv = client.get('/admin/login')
    assert rv.status_code == 200
    assert b"Admin Login" in rv.data
