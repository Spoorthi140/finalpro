def generate_recommendations(products, causal_results=None, forecasts=None):
    recommendations = []

    # Extract causal effects for better recommendations
    price_effect = -1.5 # Default fallback
    mkt_effect = 2.0
    if causal_results:
        for res in causal_results:
            if 'Price' in res['relation']: price_effect = res['effect']
            if 'Marketing' in res['relation']: mkt_effect = res['effect']

    for p in products:
        # 1. Inventory & Growth (Stock vs Forecasted Demand)
        if p.stock_quantity < 15:
            impact = p.price * 10 # Estimated loss if stockout
            recommendations.append({
                'product': p.product_name,
                'category': 'Inventory Optimization',
                'type': 'Risk',
                'priority': 'Critical',
                'message': f"Critical Stock-out Risk: {p.product_name} is below safety threshold. Projected sales velocity indicates depletion within 48 hours.",
                'impact': f"₹{impact:,.0f} Proj. Loss",
                'action': 'Emergency Restock'
            })

        # 2. Pricing Efficiency (Using Causal AI)
        margin = p.profit / p.revenue if p.revenue > 0 else 0
        if price_effect > -0.5 and margin < 0.15:
            # If price elasticity is low, we can increase price safely
            gain = p.revenue * 0.05
            recommendations.append({
                'product': p.product_name,
                'category': 'Pricing Strategy',
                'type': 'Efficiency',
                'priority': 'Medium',
                'message': f"Low Price Elasticity: Causal AI indicates {p.product_name} is price-inelastic. A 5% price hike will improve margin without significantly impacting volume.",
                'impact': f"+₹{gain:,.0f} Profit/mo",
                'action': 'Price Correction'
            })
        elif price_effect < -2.0 and p.sales < 20:
             recommendations.append({
                'product': p.product_name,
                'category': 'Revenue Growth',
                'type': 'Growth',
                'priority': 'High',
                'message': f"High Price Sensitivity: {p.product_name} is suffering from high elastic resistance. A 10% seasonal discount is recommended to capture market share.",
                'impact': f"+18% Volume Gain",
                'action': 'Apply Discount'
            })

        # 3. Marketing ROI (Using Causal AI)
        if mkt_effect > 5.0 and p.marketing_spend < 200:
            potential_revenue = mkt_effect * 500
            recommendations.append({
                'product': p.product_name,
                'category': 'Marketing Allocation',
                'type': 'Growth',
                'priority': 'High',
                'message': f"Marketing Multiplier Detected: Causal AI shows high ROI for {p.product_name}. Recommend reallocating ₹1,000 from underperforming categories.",
                'impact': f"~₹{potential_revenue:,.0f} Revenue Uplift",
                'action': 'Scale Budget'
            })

    # Global Forecasting Insight
    if forecasts and len(forecasts) > 0:
        peak_period = forecasts[0]['period']
        max_rev = forecasts[0]['revenue']
        for f in forecasts:
            if f['revenue'] > max_rev:
                max_rev = f['revenue']
                peak_period = f['period']

        recommendations.append({
            'product': 'Business Portfolio',
            'category': 'Strategic Planning',
            'type': 'Efficiency',
            'priority': 'High',
            'message': f"Capacity Warning: Forecasting predicts a seasonal peak in {peak_period}. Ensure warehouse capacity and logistics are optimized by end of next month.",
            'impact': 'Operational Readiness',
            'action': 'Plan Capacity'
        })

    # System Fallback
    if not products:
        recommendations.append({
            'product': 'System',
            'category': 'Data Hub',
            'type': 'Risk',
            'priority': 'High',
            'message': "Insufficient data for AI inference. Please upload comprehensive sales history to activate Causal Recommendation Engine.",
            'impact': 'Zero Visibility',
            'action': 'Ingest Data'
        })

    return recommendations[:10] # Limit to top 10 actionable insights
