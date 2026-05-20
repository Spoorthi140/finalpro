import dowhy
from dowhy import CausalModel
import pandas as pd
import numpy as np

def run_causal_analysis(df):
    """
    df: DataFrame with columns ['Price', 'Marketing Spend', 'Stock Quantity', 'Sales', 'Profit']
    """
    results = []

    # Refined Causal Logic: Ensuring we handle any column naming mismatch internally
    # but the input df from routes.py already uses standard internal names.

    # 1. Price -> Sales
    try:
        model = CausalModel(
            data=df,
            treatment='Price',
            outcome='Sales',
            common_causes=['Marketing Spend']
        )
        identified_estimand = model.identify_effect()
        estimate = model.estimate_effect(identified_estimand, method_name="backdoor.linear_regression")
        results.append({
            'relation': 'Price → Sales',
            'effect': estimate.value,
            'insight': "Increasing price by $1 leads to a change of {:.2f} in units sold.".format(estimate.value)
        })
    except Exception as e:
        print(f"Error in Price->Sales: {e}")

    # 2. Marketing -> Sales
    try:
        model = CausalModel(
            data=df,
            treatment='Marketing Spend',
            outcome='Sales',
            common_causes=['Price']
        )
        identified_estimand = model.identify_effect()
        estimate = model.estimate_effect(identified_estimand, method_name="backdoor.linear_regression")
        results.append({
            'relation': 'Marketing → Sales',
            'effect': estimate.value,
            'insight': "Every $1 spent on marketing increases sales by {:.2f} units.".format(estimate.value)
        })
    except Exception as e:
        print(f"Error in Marketing->Sales: {e}")

    # 3. Stock -> Profit
    try:
        model = CausalModel(
            data=df,
            treatment='Stock Quantity',
            outcome='Profit',
            common_causes=['Price', 'Marketing Spend']
        )
        identified_estimand = model.identify_effect()
        estimate = model.estimate_effect(identified_estimand, method_name="backdoor.linear_regression")
        results.append({
            'relation': 'Stock → Profit',
            'effect': estimate.value,
            'insight': "Maintaining higher stock levels has a causal impact of {:.2f} on profit per unit.".format(estimate.value)
        })
    except Exception as e:
        print(f"Error in Stock->Profit: {e}")

    return results
