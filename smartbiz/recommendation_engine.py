def generate_recommendations(products):
    recommendations = []

    for p in products:
        # Rule 1: If stock low -> Recommend restocking
        if p.stock_quantity < 10:
            recommendations.append({
                'product': p.product_name,
                'type': 'Inventory',
                'priority': 'High',
                'message': f"Low Stock Alert: Restock {p.product_name} immediately. Current quantity: {p.stock_quantity}.",
                'action': 'Restock'
            })

        # Rule 2: If high price + low sales -> Recommend reducing price
        # Demo thresholds: Price > 50 and Sales < 20
        if p.price > 50 and p.sales < 20:
            recommendations.append({
                'product': p.product_name,
                'type': 'Pricing',
                'priority': 'Medium',
                'message': f"{p.product_name} has high pricing with low sales volume. Recommend reducing price by 5%.",
                'action': 'Adjust Price'
            })

        # Rule 3: If low marketing + low demand -> Recommend increasing marketing
        # Demo thresholds: Marketing < 100 and Sales < 15
        if p.marketing_spend < 100 and p.sales < 15:
            recommendations.append({
                'product': p.product_name,
                'type': 'Marketing',
                'priority': 'Medium',
                'message': f"{p.product_name} has low marketing spend and low demand. Recommend increasing budget.",
                'action': 'Boost Marketing'
            })

    # Add general AI strategy if no specific recs
    if not recommendations:
        recommendations.append({
            'product': 'General',
            'type': 'Strategy',
            'priority': 'Low',
            'message': "Overall performance is stable. Maintain current marketing and pricing strategies.",
            'action': 'Maintain'
        })

    return recommendations
