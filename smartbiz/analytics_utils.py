import pandas as pd
import numpy as np

def perform_rfm_analysis():
    """
    Simulates RFM Analysis (Recency, Frequency, Monetary)
    In production, this would use a real 'orders' table.
    """
    segments = [
        {'name': 'Loyal Customers', 'count': 120, 'monetary': 45000, 'color': 'success'},
        {'name': 'At Risk', 'count': 45, 'monetary': 12000, 'color': 'danger'},
        {'name': 'New Customers', 'count': 85, 'monetary': 8000, 'color': 'info'},
        {'name': 'VIP (High Spenders)', 'count': 30, 'monetary': 150000, 'color': 'warning'},
    ]
    return segments

def calculate_customer_lifetime_value():
    """
    Simulated CLV Logic
    """
    avg_purchase_value = 150
    avg_purchase_freq = 4 # per year
    customer_lifespan = 3 # years
    clv = avg_purchase_value * avg_purchase_freq * customer_lifespan
    return round(clv, 2)

def retention_analysis():
    """
    Mocked monthly retention data
    """
    return [95, 92, 88, 85, 84, 82]
