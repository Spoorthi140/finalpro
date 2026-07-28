import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_absolute_error
import xgboost as xgb
import warnings
import hashlib
import threading
warnings.filterwarnings('ignore')

_forecast_cache_lock = threading.Lock()
_forecast_cache = {}

def get_products_hash(products_data):
    if not products_data:
        return "empty"
    count = len(products_data)
    stock_sum = 0
    sales_sum = 0
    revenue_sum = 0
    max_updated = ""
    for p in products_data:
        if hasattr(p, 'stock_quantity'):
            stock_sum += (p.stock_quantity or 0)
            sales_sum += (p.sales or 0)
            revenue_sum += (p.revenue or 0)
            if hasattr(p, 'last_updated') and p.last_updated:
                max_updated = max(max_updated, str(p.last_updated))
        elif isinstance(p, dict):
            stock_sum += (p.get('stock_quantity') or p.get('Stock') or 0)
            sales_sum += (p.get('sales') or p.get('Sales') or 0)
            revenue_sum += (p.get('revenue') or p.get('Revenue') or 0)
            if 'last_updated' in p:
                max_updated = max(max_updated, str(p['last_updated']))
    state_str = f"{count}-{stock_sum}-{sales_sum}-{revenue_sum}-{max_updated}"
    return hashlib.md5(state_str.encode('utf-8')).hexdigest()

try:
    from prophet import Prophet
except ImportError:
    Prophet = None

try:
    import pmdarima as pm
except ImportError:
    pm = None

def calculate_metrics(y_true, y_pred):
    """
    Computes mathematical accuracy metrics:
    - Confidence score: based on normalized MAE
    - R-squared (R2): coefficient of determination
    - MAPE: Mean Absolute Percentage Error
    - Std Error of Residuals: used for confidence intervals
    """
    y_true = np.array(y_true)
    y_pred = np.array(y_pred)
    if len(y_true) < 2:
        return 0.85, 0.0, 0.0, 0.0

    mae = mean_absolute_error(y_true, y_pred)
    mean_val = np.mean(y_true)

    # MAPE
    non_zero = y_true != 0
    if np.sum(non_zero) > 0:
        mape = np.mean(np.abs((y_true[non_zero] - y_pred[non_zero]) / y_true[non_zero])) * 100
    else:
        mape = 0.0

    # R2
    ss_tot = np.sum((y_true - mean_val) ** 2)
    ss_res = np.sum((y_true - y_pred) ** 2)
    r2 = 1 - (ss_res / ss_tot) if ss_tot > 0 else 0.0
    r2 = max(0.0, min(0.99, r2))

    # Confidence Score (percentage scale)
    confidence = 1 - (mae / mean_val) if mean_val > 0 else 0.5
    confidence = max(0.1, min(0.98, confidence))

    # Standard Error of Residuals
    residuals = y_true - y_pred
    std_err = np.std(residuals)

    return confidence, r2, mape, std_err

def get_prophet_forecast(df, target_col, periods=24):
    if not Prophet or len(df) < 2: return None, 0, 0, 0, 0
    try:
        pdf = df[['Date', target_col]].rename(columns={'Date': 'ds', target_col: 'y'})
        m = Prophet(yearly_seasonality=True, daily_seasonality=False, weekly_seasonality=False)
        m.fit(pdf)
        history = m.predict(pdf)
        conf, r2, mape, std_err = calculate_metrics(pdf['y'], history['yhat'])
        future = m.make_future_dataframe(periods=periods, freq='ME')
        forecast = m.predict(future)
        tail_fc = forecast[['ds', 'yhat']].tail(periods)
        tail_fc['yhat_lower'] = tail_fc['yhat'] - 1.96 * std_err
        tail_fc['yhat_upper'] = tail_fc['yhat'] + 1.96 * std_err
        return tail_fc, conf, r2, mape, std_err
    except:
        return None, 0, 0, 0, 0

def get_arima_forecast(df, target_col, periods=24):
    if not pm or len(df) < 10: return None, 0, 0, 0, 0
    try:
        model = pm.auto_arima(df[target_col], seasonal=True, m=12, error_action='ignore', suppress_warnings=True)
        history = model.predict_in_sample()
        conf, r2, mape, std_err = calculate_metrics(df[target_col], history)
        forecast = model.predict(n_periods=periods)
        dates = pd.date_range(start=df['Date'].max() + pd.Timedelta(days=30), periods=periods, freq='ME')
        tail_fc = pd.DataFrame({'ds': dates, 'yhat': forecast})
        tail_fc['yhat_lower'] = tail_fc['yhat'] - 1.96 * std_err
        tail_fc['yhat_upper'] = tail_fc['yhat'] + 1.96 * std_err
        return tail_fc, conf, r2, mape, std_err
    except:
        return None, 0, 0, 0, 0

def get_ensemble_forecast(df, target_col, periods=24):
    p_forecast, p_conf, p_r2, p_mape, p_std = get_prophet_forecast(df, target_col, periods)
    a_forecast, a_conf, a_r2, a_mape, a_std = get_arima_forecast(df, target_col, periods)

    if p_forecast is not None and a_forecast is not None:
        total_conf = p_conf + a_conf
        p_weight = p_conf / total_conf
        a_weight = a_conf / total_conf
        combined = p_forecast.copy()
        combined['yhat'] = (p_forecast['yhat'].values * p_weight) + (a_forecast['yhat'].values * a_weight)
        combined['yhat_lower'] = (p_forecast['yhat_lower'].values * p_weight) + (a_forecast['yhat_lower'].values * a_weight)
        combined['yhat_upper'] = (p_forecast['yhat_upper'].values * p_weight) + (a_forecast['yhat_upper'].values * a_weight)

        conf = (p_conf * p_weight + a_conf * a_weight)
        r2 = (p_r2 * p_weight + a_r2 * a_weight)
        mape = (p_mape * p_weight + a_mape * a_weight)

        return combined, conf, r2, mape, f"Prophet({p_weight:.1f})+ARIMA({a_weight:.1f}) Ensemble"
    elif p_forecast is not None:
        return p_forecast, p_conf, p_r2, p_mape, "Prophet"
    elif a_forecast is not None:
        return a_forecast, a_conf, a_r2, a_mape, "ARIMA"
    else:
        df = df.copy()
        df['days'] = (df['Date'] - df['Date'].min()).dt.days
        reg = LinearRegression().fit(df[['days']], df[target_col])
        history = reg.predict(df[['days']])
        conf, r2, mape, std_err = calculate_metrics(df[target_col], history)
        dates = pd.date_range(start=df['Date'].max() + pd.Timedelta(days=30), periods=periods, freq='ME')
        future_days = np.array([(d - df['Date'].min()).days for d in dates]).reshape(-1, 1)
        forecast = reg.predict(future_days)

        tail_fc = pd.DataFrame({'ds': dates, 'yhat': forecast})
        tail_fc['yhat_lower'] = tail_fc['yhat'] - 1.96 * std_err
        tail_fc['yhat_upper'] = tail_fc['yhat'] + 1.96 * std_err
        return tail_fc, conf, r2, mape, "Linear Regression (Trend)"

def train_and_predict(products_data, input_price, input_marketing, input_stock):
    if len(products_data) < 5:
        return None
    df = pd.DataFrame([{
        'Price': p.price, 'Marketing': p.marketing_spend, 'Stock': p.stock_quantity,
        'Sales': p.sales, 'Profit': p.profit
    } for p in products_data])
    X = df[['Price', 'Marketing', 'Stock']]
    y_sales = df['Sales']; y_profit = df['Profit']
    if len(df) >= 20:
        model_sales = xgb.XGBRegressor(n_estimators=100, learning_rate=0.1, random_state=42)
        model_profit = xgb.XGBRegressor(n_estimators=100, learning_rate=0.1, random_state=42)
    elif len(df) >= 10:
        model_sales = RandomForestRegressor(n_estimators=100, random_state=42)
        model_profit = RandomForestRegressor(n_estimators=100, random_state=42)
    else:
        model_sales = LinearRegression(); model_profit = LinearRegression()
    model_sales.fit(X, y_sales); model_profit.fit(X, y_profit)
    X_pred = pd.DataFrame([[input_price, input_marketing, input_stock]], columns=['Price', 'Marketing', 'Stock'])
    pred_sales = model_sales.predict(X_pred)[0]; pred_profit = model_profit.predict(X_pred)[0]
    avg_daily_sales = max(pred_sales / 30, 0.01)
    stock_out_days = input_stock / avg_daily_sales
    return {
        'predicted_sales': round(float(pred_sales), 2),
        'predicted_profit': round(float(pred_profit), 2),
        'stock_out_days': round(float(stock_out_days), 1)
    }

def generate_forecasts(products_data, force_refresh=False):
    if not products_data:
        return {}

    db_hash = get_products_hash(products_data)
    with _forecast_cache_lock:
        if not force_refresh and db_hash in _forecast_cache:
            return _forecast_cache[db_hash]

    df = pd.DataFrame([{
        'Date': p.date, 'Revenue': p.revenue, 'Sales': p.sales, 'Profit': p.profit,
        'Product Name': p.product_name, 'Stock': p.stock_quantity, 'Price': p.price
    } for p in products_data])
    df['Date'] = pd.to_datetime(df['Date'])
    portfolio_ts = df.groupby('Date')[['Revenue', 'Sales', 'Profit']].sum().reset_index().sort_values('Date')

    # Generate 24-month (2-year) forecasts covering 2027-2028
    periods = 24
    rev_forecast, rev_conf, rev_r2, rev_mape, rev_model = get_ensemble_forecast(portfolio_ts, 'Revenue', periods)
    sales_forecast, sales_conf, sales_r2, sales_mape, sales_model = get_ensemble_forecast(portfolio_ts, 'Sales', periods)
    profit_forecast, profit_conf, profit_r2, profit_mape, profit_model = get_ensemble_forecast(portfolio_ts, 'Profit', periods)

    # Monthly predictions with confidence intervals
    monthly = []
    for i in range(periods):
        monthly.append({
            'period': rev_forecast.iloc[i]['ds'].strftime('%b %Y'),
            'revenue': max(0.0, float(rev_forecast.iloc[i]['yhat'])),
            'revenue_lower': max(0.0, float(rev_forecast.iloc[i]['yhat_lower'])),
            'revenue_upper': max(0.0, float(rev_forecast.iloc[i]['yhat_upper'])),
            'sales': max(0.0, float(sales_forecast.iloc[i]['yhat'])),
            'sales_lower': max(0.0, float(sales_forecast.iloc[i]['yhat_lower'])),
            'sales_upper': max(0.0, float(sales_forecast.iloc[i]['yhat_upper'])),
            'profit': max(0.0, float(profit_forecast.iloc[i]['yhat'])),
            'profit_lower': max(0.0, float(profit_forecast.iloc[i]['yhat_lower'])),
            'profit_upper': max(0.0, float(profit_forecast.iloc[i]['yhat_upper'])),
            'demand': max(0, float(sales_forecast.iloc[i]['yhat']) * 1.1),
            'inventory_req': max(0, float(sales_forecast.iloc[i]['yhat']) * 1.25)
        })

    # Quarterly (Group monthly forecasts)
    quarterly = []
    temp_df = pd.DataFrame(monthly)
    temp_df['ds'] = pd.to_datetime(temp_df['period'])
    q_df = temp_df.set_index('ds')[['revenue', 'sales', 'profit']].resample('QE').sum().reset_index()
    for _, row in q_df.iterrows():
        quarterly.append({
            'period': f"Q{row['ds'].quarter} {row['ds'].year}",
            'revenue': float(row['revenue']),
            'sales': float(row['sales']),
            'profit': float(row['profit'])
        })

    # Yearly
    yearly = []
    y_df = temp_df.set_index('ds')[['revenue', 'sales', 'profit']].resample('YE').sum().reset_index()
    for _, row in y_df.iterrows():
        yearly.append({
            'period': f"{row['ds'].year}",
            'revenue': float(row['revenue']),
            'sales': float(row['sales']),
            'profit': float(row['profit'])
        })

    # Product-level forecasting and stock-out risk
    product_forecasts = []

    for product_name in df['Product Name'].unique():
        p_df = df[df['Product Name'] == product_name].sort_values('Date')
        current_stock = p_df['Stock'].iloc[-1]
        current_sales = p_df['Sales'].iloc[-1]

        # Simple trend per product
        if len(p_df) >= 2:
            growth = (p_df['Sales'].iloc[-1] / p_df['Sales'].iloc[0]) ** (1/len(p_df))
        else:
            growth = 1.05

        forecasted_demand = current_sales * growth
        # Stock-out risk calculation
        avg_daily_sales = max(current_sales / 30, 0.01)
        days_until_stockout = current_stock / avg_daily_sales
        risk_level = "High" if days_until_stockout < 7 else "Medium" if days_until_stockout < 15 else "Low"

        product_forecasts.append({
            'product_name': product_name,
            'current_sales': int(current_sales),
            'forecasted_demand': int(forecasted_demand),
            'growth_pct': round((growth - 1) * 100, 1),
            'current_stock': int(current_stock),
            'required_inventory': int(forecasted_demand * 1.2),
            'days_until_stockout': round(days_until_stockout, 1),
            'risk_level': risk_level
        })

    avg_confidence = (rev_conf + sales_conf + profit_conf) / 3
    avg_r2 = (rev_r2 + sales_r2 + profit_r2) / 3
    avg_mape = (rev_mape + sales_mape + profit_mape) / 3

    result = {
        'monthly': monthly,
        'quarterly': quarterly,
        'yearly': yearly,
        'product_forecasts': product_forecasts,
        'model_info': f"Revenue: {rev_model}",
        'confidence_score': round(avg_confidence * 100, 1),
        'r2_score': round(avg_r2, 4),
        'mape_score': round(avg_mape, 2)
    }
    with _forecast_cache_lock:
        _forecast_cache[db_hash] = result
        if len(_forecast_cache) > 5:
            first_key = next(iter(_forecast_cache))
            _forecast_cache.pop(first_key, None)
    return result
