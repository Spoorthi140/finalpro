import pandas as pd
import numpy as np
from smartbiz.prediction import generate_forecasts
from smartbiz.causal_analysis import run_causal_analysis
from smartbiz.recommendation_engine import generate_recommendations

class MockProduct:
    def __init__(self, date, revenue, sales, profit, product_name="Test Product", stock_quantity=100, price=50.0, marketing_spend=100):
        self.date = date
        self.revenue = revenue
        self.sales = sales
        self.profit = profit
        self.product_name = product_name
        self.stock_quantity = stock_quantity
        self.price = price
        self.marketing_spend = marketing_spend

def test_generate_forecasts_logic():
    # Create mock data with at least 2 points for growth calculation
    data = [
        MockProduct('2023-01-01', 1000, 100, 200),
        MockProduct('2023-02-01', 1100, 110, 220),
        MockProduct('2023-03-01', 1200, 120, 240)
    ]

    forecasts = generate_forecasts(data)

    assert 'monthly' in forecasts
    assert 'quarterly' in forecasts
    assert 'yearly' in forecasts
    assert 'product_forecasts' in forecasts
    assert len(forecasts['monthly']) == 12
    assert forecasts['confidence_score'] > 0
    assert forecasts['monthly'][0]['revenue'] > 0
    assert forecasts['product_forecasts'][0]['product_name'] == "Test Product"

def test_run_causal_analysis():
    df = pd.DataFrame({
        'Price': [10, 12, 11, 13, 12, 14, 13, 15, 14, 16],
        'Sales': [100, 90, 95, 85, 92, 80, 88, 75, 82, 70],
        'Marketing Spend': [20, 25, 22, 30, 28, 35, 32, 40, 38, 45],
        'Revenue': [1000, 1080, 1045, 1105, 1104, 1120, 1144, 1125, 1148, 1120],
        'Stock Quantity': [50, 50, 55, 55, 60, 60, 65, 65, 70, 70],
        'Profit': [200, 220, 210, 240, 230, 260, 250, 280, 270, 300]
    })

    results = run_causal_analysis(df)
    assert len(results) > 0
    assert any(r['relation'] == 'Price → Sales' for r in results)
    assert any(r['relation'] == 'Marketing → Revenue' for r in results)

def test_generate_recommendations():
    # Mock data
    products = [
        MockProduct('2023-01-01', 1000, 10, 200, "Sensitive Product", 5, 100),
        MockProduct('2023-01-01', 5000, 50, 1000, "Growth Product", 50, 100)
    ]
    causal = [
        {'relation': 'Price → Sales', 'effect': -5.0, 'confidence': 0.9, 'insight': 'test'},
        {'relation': 'Marketing → Revenue', 'effect': 10.0, 'confidence': 0.9, 'insight': 'test'}
    ]
    forecasts = {
        'product_forecasts': [
            {'product_name': 'Sensitive Product', 'risk_level': 'High', 'days_until_stockout': 2, 'current_stock': 5, 'forecasted_demand': 50}
        ]
    }

    recs = generate_recommendations(products, causal, forecasts)
    assert len(recs) > 0
    # Check for High Price Elasticity rec (Sensitive Product has price_effect -5.0 and sales 10 < 20)
    assert any(r['category'] == 'Pricing Optimization' for r in recs)
    # Check for Inventory Risk rec
    assert any(r['category'] == 'Inventory Risk' for r in recs)
