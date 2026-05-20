import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.linear_model import LinearRegression

def train_and_predict(products_data, input_price, input_marketing, input_stock):
    """
    products_data: list of product objects from DB
    """
    if len(products_data) < 2:
        return None

    df = pd.DataFrame([{
        'Price': p.price,
        'Marketing': p.marketing_spend,
        'Stock': p.stock_quantity,
        'Sales': p.sales,
        'Profit': p.profit
    } for p in products_data])

    X = df[['Price', 'Marketing', 'Stock']]
    y_sales = df['Sales']
    y_profit = df['Profit']

    # Using Random Forest if enough data, else Linear Regression
    if len(df) >= 10:
        model_sales = RandomForestRegressor(n_estimators=100, random_state=42)
        model_profit = RandomForestRegressor(n_estimators=100, random_state=42)
    else:
        model_sales = LinearRegression()
        model_profit = LinearRegression()

    model_sales.fit(X, y_sales)
    model_profit.fit(X, y_profit)

    X_pred = np.array([[input_price, input_marketing, input_stock]])
    pred_sales = model_sales.predict(X_pred)[0]
    pred_profit = model_profit.predict(X_pred)[0]

    # Calculate stock-out days
    # Formula: Stock Quantity / Average Daily Sales
    # Assuming Sales in DB is for last 30 days
    avg_daily_sales = max(pred_sales / 30, 0.01)
    stock_out_days = input_stock / avg_daily_sales

    return {
        'predicted_sales': round(pred_sales, 2),
        'predicted_profit': round(pred_profit, 2),
        'stock_out_days': round(stock_out_days, 1)
    }
