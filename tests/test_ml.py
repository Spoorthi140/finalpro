import pandas as pd
import numpy as np
from smartbiz.prediction import generate_forecasts

class MockProduct:
    def __init__(self, date, revenue, sales, profit):
        self.date = date
        self.revenue = revenue
        self.sales = sales
        self.profit = profit

def test_generate_forecasts_logic():
    # Create mock data
    data = [
        MockProduct('2023-01-01', 1000, 100, 200),
        MockProduct('2023-02-01', 1100, 110, 220),
        MockProduct('2023-03-01', 1200, 120, 240)
    ]

    forecasts = generate_forecasts(data)
    assert len(forecasts) == 6
    assert 'period' in forecasts[0]
    assert 'revenue' in forecasts[0]
    assert forecasts[0]['revenue'] > 0
