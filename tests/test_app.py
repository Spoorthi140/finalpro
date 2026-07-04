import pytest
from smartbiz.app import create_app
from smartbiz.models import db

@pytest.fixture
def app():
    app = create_app({
        'TESTING': True,
        'SQLALCHEMY_DATABASE_URI': 'sqlite:///:memory:',
        'WTF_CSRF_ENABLED': False
    })
    with app.app_context():
        db.create_all()
    yield app

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

def test_admin_redirect(client):
    rv = client.get('/admin/login')
    assert rv.status_code == 302
    assert b'href="/login"' in rv.data
