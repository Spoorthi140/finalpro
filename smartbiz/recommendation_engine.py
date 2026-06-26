def generate_recommendations(products, causal_results=None, forecasts=None):
    recommendations = []

    # Dynamic Causal Coefficients from DoWhy results
    price_effect = 0
    mkt_effect = 0
    stock_effect = 0

    if causal_results:
        for res in causal_results:
            if 'Price' in res['relation']: price_effect = res['effect']
            if 'Marketing' in res['relation']: mkt_effect = res['effect']
            if 'Stock' in res['relation']: stock_effect = res['effect']

    # Rule-based generation with prioritized categories
    for p in products:
        # Pricing Recommendations (Causal)
        if price_effect < -2.0 and p.sales < 20: # High price sensitivity
            impact_rev = p.revenue * 0.15
            recommendations.append({
                'product': p.product_name,
                'category': 'Pricing Optimization',
                'type': 'Growth',
                'priority': 'High',
                'message': f"High Price Elasticity Detected ({price_effect:.2f}). Reducing price for {p.product_name} by 5-10% is projected to break demand resistance and increase volume.",
                'impact': f"₹{impact_rev:,.0f} Proj. Revenue Gain",
                'action': 'Reduce Price',
                'effort': 'Low',
                'impact_score': 85
            })
        elif price_effect > -0.5 and p.profit / p.revenue < 0.1: # Low sensitivity, low margin
            impact_profit = p.revenue * 0.05
            recommendations.append({
                'product': p.product_name,
                'category': 'Margin Protection',
                'type': 'Efficiency',
                'priority': 'Medium',
                'message': f"Price Inelasticity Detected ({price_effect:.2f}). {p.product_name} can sustain a 3-5% price hike to improve thin margins without significant volume loss.",
                'impact': f"₹{impact_profit:,.0f} Proj. Profit Gain",
                'action': 'Increase Price',
                'effort': 'Low',
                'impact_score': 65
            })

        # Marketing Recommendations (Causal)
        if mkt_effect > 5.0 and p.marketing_spend < 500:
            potential_uplift = mkt_effect * 200
            recommendations.append({
                'product': p.product_name,
                'category': 'Growth Acceleration',
                'type': 'Growth',
                'priority': 'High',
                'message': f"Strong Marketing Multiplier ({mkt_effect:.2f}). Increasing marketing spend for {p.product_name} shows high causal correlation with revenue growth.",
                'impact': f"₹{potential_uplift:,.0f} Proj. Growth",
                'action': 'Scale Marketing',
                'effort': 'Medium',
                'impact_score': 90
            })

    # Inventory Recommendations (Forecasting based)
    if forecasts and 'product_forecasts' in forecasts:
        for pf in forecasts['product_forecasts']:
            if pf['risk_level'] == 'High':
                loss_mitigation = pf['forecasted_demand'] * 0.8 * 100 # Rough est
                recommendations.append({
                    'product': pf['product_name'],
                    'category': 'Inventory Risk',
                    'type': 'Risk Mitigation',
                    'priority': 'Critical',
                    'message': f"Stock-out predicted in {pf['days_until_stockout']} days. Current inventory ({pf['current_stock']}) is insufficient for forecasted demand ({pf['forecasted_demand']}).",
                    'impact': f"₹{loss_mitigation:,.0f} Risk Mitigation",
                    'action': 'Restock Now',
                    'effort': 'Medium',
                    'impact_score': 95
                })
            elif pf['current_stock'] > pf['forecasted_demand'] * 2:
                recommendations.append({
                    'product': pf['product_name'],
                    'category': 'Capital Efficiency',
                    'type': 'Efficiency',
                    'priority': 'Low',
                    'message': f"Overstock detected for {pf['product_name']}. Inventory is 2x above projected monthly demand. Reduce procurement to free up working capital.",
                    'impact': 'Capital Release',
                    'action': 'Reduce Buy',
                    'effort': 'Low',
                    'impact_score': 40
                })

    # Sort by impact score
    recommendations.sort(key=lambda x: x['impact_score'], reverse=True)

    return recommendations[:15]
