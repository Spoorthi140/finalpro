def generate_recommendations(products):
    recommendations = []

    for p in products:
        # 1. Low Stock Rule
        if p.stock_quantity < 10:
            recommendations.append({
                'product': p.product_name,
                'type': 'Inventory',
                'priority': 'High',
                'message': f"Critical: {p.product_name} is low on stock ({p.stock_quantity}). Restock immediately within 3 days.",
                'action': 'Restock'
            })

        # 2. Pricing Rule (High price, low sales)
        # Assuming 20 is a low sales threshold for demo
        if p.price > 50 and p.sales < 15:
            recommendations.append({
                'product': p.product_name,
                'type': 'Pricing',
                'priority': 'Medium',
                'message': f"{p.product_name} has high pricing with low sales volume. Recommend reducing price by 10% to stimulate demand.",
                'action': 'Adjust Price'
            })

        # 3. Marketing Rule (Low marketing, low sales)
        if p.marketing_spend < 100 and p.sales < 10:
            recommendations.append({
                'product': p.product_name,
                'type': 'Marketing',
                'priority': 'Medium',
                'message': f"{p.product_name} has minimal marketing exposure. Recommend increasing marketing budget by 20% to boost visibility.",
                'action': 'Boost Marketing'
            })

        # 4. Profitability Rule (High sales, low profit)
        if p.sales > 50 and p.profit < (p.sales * 2): # demo margin check
            recommendations.append({
                'product': p.product_name,
                'type': 'Strategy',
                'priority': 'High',
                'message': f"{p.product_name} has high sales but thin margins. Analyze supply chain costs to improve unit profitability.",
                'action': 'Cost Analysis'
            })

    return recommendations
