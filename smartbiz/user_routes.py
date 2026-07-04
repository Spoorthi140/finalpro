from flask import Blueprint, render_template, redirect, url_for, flash, request, current_app, send_file
from flask_login import login_user, logout_user, login_required, current_user
from werkzeug.security import generate_password_hash, check_password_hash
from werkzeug.utils import secure_filename
import os
import pandas as pd
from .models import db, User, Product, Prediction, InventoryLog, AuditLog, Company, Report
from .causal_analysis import run_causal_analysis
from .recommendation_engine import generate_recommendations
from .prediction import train_and_predict, generate_forecasts
from .report_generator import generate_pdf_report, generate_excel_report
from .inventory_monitor import detect_objects
from .auth_utils import role_required
from .analytics_utils import perform_rfm_analysis, calculate_customer_lifetime_value, retention_analysis

user_bp = Blueprint('user', __name__)

@user_bp.route('/')
def home():
    return render_template('user/home.html')

@user_bp.route('/register', methods=['GET', 'POST'])
def register():
    if current_user.is_authenticated:
        return redirect(url_for('user.dashboard'))
    if request.method == 'POST':
        username = request.form.get('username')
        email = request.form.get('email')
        password = request.form.get('password')

        user_exists = User.query.filter_by(email=email).first()
        if user_exists:
            flash('Email already exists.', 'danger')
            return redirect(url_for('user.register'))

        new_user = User(
            username=username, email=email,
            password=generate_password_hash(password, method='pbkdf2:sha256'),
            role='USER' # Force USER role for all registrations
        )
        db.session.add(new_user)
        db.session.commit()
        flash('Registration successful! Please login.', 'success')
        return redirect(url_for('user.login'))
    return render_template('user/register.html')

@user_bp.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('user.dashboard'))
    if request.method == 'POST':
        email = request.form.get('email')
        password = request.form.get('password')
        user = User.query.filter_by(email=email).first()
        if user and check_password_hash(user.password, password):
            login_user(user)
            return redirect(url_for('user.dashboard'))
        else:
            flash('Login failed. Check email and password.', 'danger')
    return render_template('user/login.html')

@user_bp.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('user.home'))

@user_bp.route('/dashboard')
@login_required
def dashboard():
    products = Product.query.all()
    total_products = len(products)
    total_sales = sum([p.sales for p in products])
    total_profit = sum([p.profit for p in products])
    total_revenue = sum([p.revenue for p in products])
    total_inventory = sum([p.stock_quantity for p in products])
    total_reports = Report.query.count()
    total_users = User.query.count()

    low_stock_products = [p for p in products if p.stock_quantity < p.minimum_threshold]
    categories = list(set([p.category for p in products if p.category]))

    recent_activities = AuditLog.query.order_by(AuditLog.timestamp.desc()).limit(5).all()
    recent_predictions = Prediction.query.order_by(Prediction.timestamp.desc()).limit(5).all()
    recent_detections = InventoryDetection.query.order_by(InventoryDetection.timestamp.desc()).limit(5).all()

    if products:
        df = pd.DataFrame([{'Date': p.date, 'Revenue': p.revenue, 'Profit': p.profit, 'Sales': p.sales} for p in products])
        df['Date'] = pd.to_datetime(df['Date'])
        # Dynamic Trends
        monthly = df.set_index('Date').resample('ME').sum().tail(6)
        chart_labels = [d.strftime('%b') for d in monthly.index]
        chart_revenue = monthly['Revenue'].tolist()
        chart_profit = monthly['Profit'].tolist()
        chart_sales = monthly['Sales'].tolist()
    else:
        chart_labels = ['N/A']
        chart_revenue = [0]; chart_profit = [0]; chart_sales = [0]

    return render_template('user/dashboard.html',
                           total_products=total_products,
                           total_sales=total_sales,
                           total_profit=total_profit,
                           total_revenue=total_revenue,
                           total_inventory=total_inventory,
                           total_reports=total_reports,
                           total_users=total_users,
                           total_categories=len(categories),
                           low_stock_count=len(low_stock_products),
                           low_stock_items=low_stock_products[:5],
                           activities=recent_activities,
                           predictions=recent_predictions,
                           detections=recent_detections,
                           chart_labels=chart_labels,
                           chart_revenue=chart_revenue,
                           chart_profit=chart_profit,
                           chart_sales=chart_sales)

@user_bp.route('/upload', methods=['GET', 'POST'])
@login_required
@role_required(['ADMIN', 'USER'])
def upload():
    upload_results = None; preview_data = []
    if request.method == 'POST':
        if 'file' not in request.files:
            flash('No file part', 'danger'); return redirect(request.url)
        file = request.files['file']
        if file.filename == '':
            flash('No selected file', 'danger'); return redirect(request.url)
        if file and (file.filename.endswith('.csv') or file.filename.endswith('.xlsx')):
            filename = secure_filename(file.filename)
            filepath = os.path.join(current_app.config['UPLOAD_FOLDER'], filename)
            file.save(filepath)
            try:
                df = pd.read_csv(filepath) if filename.endswith('.csv') else pd.read_excel(filepath)
                required_cols = ['Product Name', 'Category', 'Price', 'Marketing Spend', 'Stock Quantity', 'Sales', 'Revenue', 'Profit', 'Date']
                missing_cols = [col for col in required_cols if col not in df.columns]
                if missing_cols:
                    flash(f'Missing columns: {", ".join(missing_cols)}', 'danger'); return redirect(request.url)
                total_records = len(df)
                missing_values = df.isnull().sum().sum()
                duplicates_removed = total_records - len(df.drop_duplicates())
                df = df.drop_duplicates().fillna(0)
                outliers = 0
                for col in ['Price', 'Sales', 'Profit']:
                    if df[col].std() > 0:
                        z_scores = (df[col] - df[col].mean()) / df[col].std()
                        outliers += (z_scores.abs() > 3).sum()
                quality_score = min(100, max(0, 100 - (missing_values * 2) - (duplicates_removed * 1) - (outliers * 5)))
                for _, row in df.iterrows():
                    p = Product.query.filter_by(product_name=row['Product Name']).first()
                    if p:
                        p.category = row['Category']; p.price = row['Price']
                        p.marketing_spend = row['Marketing Spend']; p.stock_quantity = row['Stock Quantity']
                        p.sales = row['Sales']; p.revenue = row['Revenue']; p.profit = row['Profit']
                        p.date = pd.to_datetime(row['Date'])
                    else:
                        new_p = Product(
                            product_name=row['Product Name'], category=row['Category'],
                            price=row['Price'], marketing_spend=row['Marketing Spend'],
                            stock_quantity=row['Stock Quantity'], sales=row['Sales'],
                            revenue=row['Revenue'], profit=row['Profit'], date=pd.to_datetime(row['Date'])
                        )
                        db.session.add(new_p)
                db.session.commit()
                upload_results = {
                    'total_records': total_records, 'missing_values': missing_values,
                    'duplicates_removed': duplicates_removed, 'outliers_detected': outliers,
                    'quality_score': quality_score, 'status': 'Dataset Uploaded and Processed Successfully'
                }
                preview_data = df.head(10).to_dict('records')
                flash('Data analysis and validation complete!', 'success')
            except Exception as e:
                flash(f'Error processing file: {str(e)}', 'danger'); return redirect(request.url)
    return render_template('user/upload.html', upload_results=upload_results, preview_data=preview_data)

@user_bp.route('/inventory')
@login_required
@role_required(['ADMIN', 'USER'])
def inventory():
    products = Product.query.all()
    return render_template('user/inventory.html', products=products)

@user_bp.route('/simulator', methods=['GET', 'POST'])
@login_required
@role_required(['ADMIN', 'USER'])
def simulator():
    products = Product.query.all()
    simulation_result = None
    if request.method == 'POST':
        pc = float(request.form.get('price_change', 0)) / 100
        mc = float(request.form.get('marketing_change', 0)) / 100
        cr = sum([p.revenue for p in products]); cp = sum([p.profit for p in products]); cs = sum([p.sales for p in products])
        ss = cs * (1 - 1.5 * pc) * (1 + 0.5 * mc)
        sr = cr * (1 + pc) * (ss / cs if cs > 0 else 1)
        sp = sr * (cp / cr if cr > 0 else 0.2)
        simulation_result = {
            'current_sales': round(cs, 2), 'current_revenue': round(cr, 2), 'current_profit': round(cp, 2),
            'simulated_sales': round(ss, 2), 'simulated_revenue': round(sr, 2), 'simulated_profit': round(sp, 2),
            'sales_impact': round(((ss - cs) / cs * 100), 2) if cs > 0 else 0,
            'revenue_impact': round(((sr - cr) / cr * 100), 2) if cr > 0 else 0,
            'profit_impact': round(((sp - cp) / cp * 100), 2) if cp > 0 else 0,
        }
    return render_template('user/simulator.html', simulation=simulation_result)

@user_bp.route('/forecasting')
@login_required
@role_required(['ADMIN', 'USER'])
def forecasting():
    products = Product.query.all()
    if not products:
        flash("No data available for forecasting. Please upload a dataset.", "warning")
        return redirect(url_for('user.upload'))
    forecasts = generate_forecasts(products)

    # Historical data for combined charts
    df = pd.DataFrame([{'Date': p.date, 'Revenue': p.revenue, 'Sales': p.sales} for p in products])
    df['Date'] = pd.to_datetime(df['Date'])
    hist_monthly = df.set_index('Date').resample('ME').sum().tail(6).reset_index()
    historical = {
        'labels': [d.strftime('%b %Y') for d in hist_monthly['Date']],
        'revenue': hist_monthly['Revenue'].tolist(),
        'sales': hist_monthly['Sales'].tolist()
    }

    return render_template('user/forecasting.html', forecasts=forecasts, historical=historical)

@user_bp.route('/customer_intelligence')
@login_required
@role_required(['ADMIN', 'USER'])
def customer_intelligence():
    segments = perform_rfm_analysis(); clv = calculate_customer_lifetime_value(); retention = retention_analysis()
    return render_template('user/customer_intelligence.html', segments=segments, clv=clv, retention=retention)

@user_bp.route('/prediction', methods=['GET', 'POST'])
@login_required
@role_required(['ADMIN', 'USER'])
def prediction():
    prediction_result = None
    if request.method == 'POST':
        p, m, s = float(request.form.get('price', 0)), float(request.form.get('marketing', 0)), float(request.form.get('stock', 0))
        products = Product.query.all()
        prediction_result = train_and_predict(products, p, m, s)
        if prediction_result:
            db.session.add(Prediction(product_id=products[0].id if products else None, predicted_sales=prediction_result['predicted_sales'], predicted_profit=prediction_result['predicted_profit'], stock_out_days=prediction_result['stock_out_days']))
            db.session.commit()
    return render_template('user/prediction.html', prediction=prediction_result)

@user_bp.route('/causal_analysis')
@login_required
@role_required(['ADMIN', 'USER'])
def causal_analysis():
    products = Product.query.all()
    if not products: flash("No data available for analysis.", "warning"); return redirect(url_for('user.upload'))
    df = pd.DataFrame([{'Product Name': p.product_name, 'Category': p.category, 'Price': p.price, 'Marketing Spend': p.marketing_spend, 'Stock Quantity': p.stock_quantity, 'Sales': p.sales, 'Revenue': p.revenue, 'Profit': p.profit, 'Date': p.date} for p in products])
    causal_results = run_causal_analysis(df) if len(df) >= 5 else []
    return render_template('user/causal_analysis.html', causal_results=causal_results)

@user_bp.route('/recommendations')
@login_required
@role_required(['ADMIN', 'USER'])
def recommendations():
    products = Product.query.all()
    if not products: flash("No data available. Please upload a dataset.", "warning"); return redirect(url_for('user.upload'))
    df = pd.DataFrame([{'Product Name': p.product_name, 'Category': p.category, 'Price': p.price, 'Marketing Spend': p.marketing_spend, 'Stock Quantity': p.stock_quantity, 'Sales': p.sales, 'Revenue': p.revenue, 'Profit': p.profit, 'Date': p.date} for p in products])
    causal_results = run_causal_analysis(df) if len(df) >= 5 else []
    forecasts = generate_forecasts(products)
    recs = generate_recommendations(products, causal_results=causal_results, forecasts=forecasts)

    # Aggregated data for recommendation visualizations
    rec_dist = {'Pricing': 0, 'Marketing': 0, 'Inventory': 0, 'Efficiency': 0, 'Strategic': 0}
    for r in recs:
        cat = r['category'].split()[0]
        if cat in rec_dist: rec_dist[cat] += 1
        elif 'Capital' in r['category']: rec_dist['Efficiency'] += 1
        else: rec_dist['Strategic'] += 1

    # Convert products to serializable dicts for Chart.js
    products_list = [{
        'id': p.id,
        'product_name': p.product_name,
        'price': p.price,
        'sales': p.sales,
        'marketing_spend': p.marketing_spend,
        'revenue': p.revenue
    } for p in products]

    return render_template('user/recommendations.html', recommendations=recs, products=products_list, causal_results=causal_results, rec_dist=rec_dist)

@user_bp.route('/reports')
@login_required
@role_required(['ADMIN', 'USER'])
def reports():
    search = request.args.get('q')
    query = Report.query
    if search:
        query = query.filter(Report.report_name.contains(search))
    recent_reports = query.order_by(Report.created_at.desc()).all()
    return render_template('user/reports.html', recent_reports=recent_reports)

@user_bp.route('/download_report/<format>')
@login_required
@role_required(['ADMIN', 'USER'])
def download_report(format):
    products = Product.query.all()
    ts, tp, tr = sum([p.sales for p in products]), sum([p.profit for p in products]), sum([p.revenue for p in products])
    if format == 'pdf':
        buffer = generate_pdf_report(products, ts, tp, tr)
        # Log the report generation
        new_report = Report(
            report_name=f"SmartBiz_Executive_{pd.Timestamp.now().strftime('%Y%m%d_%H%M')}.pdf",
            report_type="Executive",
            created_by=current_user.id
        )
        db.session.add(new_report)
        db.session.commit()
        buffer.seek(0)
        return send_file(buffer, as_attachment=True, download_name=new_report.report_name, mimetype='application/pdf')
    elif format == 'excel':
        buffer = generate_excel_report(products)
        # Log the report generation
        new_report = Report(
            report_name=f"SmartBiz_Data_{pd.Timestamp.now().strftime('%Y%m%d_%H%M')}.xlsx",
            report_type="Inventory",
            created_by=current_user.id
        )
        db.session.add(new_report)
        db.session.commit()
        buffer.seek(0)
        return send_file(buffer, as_attachment=True, download_name=new_report.report_name, mimetype='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')
    return redirect(url_for('user.reports'))

@user_bp.route('/inventory/detect', methods=['POST'])
@login_required
def inventory_detect():
    if 'file' not in request.files:
        return {'error': 'No file'}, 400
    file = request.files['file']
    if file.filename == '':
        return {'error': 'No selected file'}, 400

    # Read file content into memory for OpenCV
    file_bytes = file.read()
    import base64
    encoded = base64.b64encode(file_bytes).decode('utf-8')
    image_data = f"data:image/jpeg;base64,{encoded}"

    count, annotated_filename, confidence = detect_objects(image_data)

    # Store in DB
    detection = InventoryDetection(
        image_name=file.filename,
        annotated_path=annotated_filename,
        box_count=count,
        average_confidence=confidence
    )
    db.session.add(detection)
    db.session.add(AuditLog(user_id=current_user.id, action=f"AI Detection: Found {count} boxes in {file.filename}", module="Inventory"))
    db.session.commit()

    return {
        'count': count,
        'annotated': annotated_filename,
        'confidence': confidence
    }

@user_bp.route('/inventory/detect_sample', methods=['POST'])
@login_required
def detect_sample():
    data = request.get_json()
    filename = data.get('filename')
    sample_path = os.path.join(current_app.config['SAMPLE_IMAGES_FOLDER'], filename)

    if not os.path.exists(sample_path):
        return {'error': 'Sample image not found'}, 404

    count, annotated_filename, confidence = detect_objects(sample_path, is_path=True)

    detection = InventoryDetection(
        image_name=f"Sample: {filename}",
        annotated_path=annotated_filename,
        box_count=count,
        average_confidence=confidence
    )
    db.session.add(detection)
    db.session.commit()

    return {
        'count': count,
        'annotated': annotated_filename,
        'confidence': confidence
    }

@user_bp.route('/update_inventory', methods=['POST'])
@login_required
def update_inventory():
    data = request.get_json(); pid = data.get('product_id'); qty = int(data.get('quantity'))
    product = Product.query.get(pid)
    if product:
        product.stock_quantity = qty
        db.session.add(InventoryLog(product_id=pid, detected_quantity=qty))
        db.session.commit()
        return {'success': True}
    return {'success': False}
