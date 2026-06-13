def generate_recommendations(products):
    recommendations = []

    for p in products:
        # Rule 1: Inventory Management
        if p.stock_quantity < 10:
            recommendations.append({
                'product': p.product_name,
                'category': 'Inventory',
                'priority': 'High',
                'message': f"Low Stock Alert: {p.product_name} has only {p.stock_quantity} units remaining. Restock immediately.",
                'action': 'Restock'
            })

        # Rule 2: Pricing Strategy (Causal-based)
        # If profit margin is low but price is competitive, maybe increase price?
        margin = p.profit / p.revenue if p.revenue > 0 else 0
        if margin < 0.1 and p.revenue > 0:
             recommendations.append({
                'product': p.product_name,
                'category': 'Pricing',
                'priority': 'Medium',
                'message': f"Low Margin Detected: {p.product_name} margin is only {margin*100:.1f}%. Consider a 5% price increase.",
                'action': 'Optimize Price'
            })
        elif p.sales < 10 and p.price > 100:
            recommendations.append({
                'product': p.product_name,
                'category': 'Pricing',
                'priority': 'Medium',
                'message': f"High Price / Low Sales: {p.product_name} is underperforming. Recommend a promotional discount.",
                'action': 'Discount'
            })

        # Rule 3: Marketing Budget
        if p.marketing_spend < 50 and p.sales < 20:
            recommendations.append({
                'product': p.product_name,
                'category': 'Marketing',
                'priority': 'Low',
                'message': f"Under-exposed: {p.product_name} has low marketing visibility. Increase budget by ₹500.",
                'action': 'Boost Marketing'
            })

    # Global Recommendations
    if not products:
        recommendations.append({
            'product': 'System',
            'category': 'Data',
            'priority': 'High',
            'message': "No business data detected. Please upload a dataset to generate insights.",
            'action': 'Upload Data'
        })
    elif len(recommendations) < 3:
        recommendations.append({
            'product': 'General',
            'category': 'Strategy',
            'priority': 'Low',
            'message': "Operations are stable. Continue monitoring seasonal trends for Q4.",
            'action': 'Monitor'
        })

    return recommendations
