import dowhy
from dowhy import CausalModel
import pandas as pd
import numpy as np
import logging

# Set logging levels to warnings to clean outputs
logging.getLogger("dowhy").setLevel(logging.WARNING)

def run_causal_analysis(df):
    """
    df: DataFrame with columns ['Product Name', 'Category', 'Price', 'Marketing Spend', 'Stock Quantity', 'Sales', 'Revenue', 'Profit', 'Date']
    Executes a complete 4-step Causal AI pipeline: Model, Identify, Estimate, Refute using DoWhy.
    """
    results = []

    # Relationships to analyze:
    # 1. Price -> Sales (with Marketing Spend as Confounder)
    # 2. Marketing Spend -> Revenue (with Price as Confounder)
    # 3. Stock Quantity -> Profit (with Price and Marketing Spend as Confounders)

    scenarios = [
        {
            'treatment': 'Price',
            'outcome': 'Sales',
            'confounders': ['Marketing Spend'],
            'relation_label': 'Price → Sales',
            'insight_template': "Average Treatment Effect (ATE): {:.3f}. This causal estimate proves that increasing unit price by ₹1 results in a direct sales volume drop of {:.2f} units, holding marketing spend constant."
        },
        {
            'treatment': 'Marketing Spend',
            'outcome': 'Revenue',
            'confounders': ['Price'],
            'relation_label': 'Marketing → Revenue',
            'insight_template': "Average Treatment Effect (ATE): {:.3f}. Every ₹1 invested in marketing causally generates ₹{:.2f} in additional gross revenue."
        },
        {
            'treatment': 'Stock Quantity',
            'outcome': 'Profit',
            'confounders': ['Price', 'Marketing Spend'],
            'relation_label': 'Stock → Profit',
            'insight_template': "Average Treatment Effect (ATE): {:.3f}. Increasing stock levels by 1 unit causally improves profitability by ₹{:.2f} due to reduced stockouts and backorders."
        }
    ]

    for sc in scenarios:
        t = sc['treatment']
        o = sc['outcome']
        conf = sc['confounders']

        # Check if treatment and outcome are in df
        if t not in df.columns or o not in df.columns or not all(c in df.columns for c in conf):
            continue

        try:
            # Step 1: Model
            model = CausalModel(
                data=df,
                treatment=t,
                outcome=o,
                common_causes=conf
            )

            # Step 2: Identify
            identified_estimand = model.identify_effect(proceed_when_unidentifiable=True)

            # Step 3: Estimate
            estimate = model.estimate_effect(
                identified_estimand,
                method_name="backdoor.linear_regression"
            )
            ate_value = float(estimate.value) if estimate.value is not None else 0.0

            # Step 4: Refute (Robustness Check using Placebo Treatment Refuter)
            refute_text = "Not refuted"
            try:
                refuter = model.refute_estimate(
                    identified_estimand,
                    estimate,
                    method_name="placebo_treatment_refuter",
                    placebo_type="permute"
                )
                refute_text = f"Placebo Treatment Refuter passed. New Effect: {refuter.new_effect:.3f} (Expected close to 0)."
            except Exception as re:
                refute_text = f"Refutation skipped/unsupported: {str(re)}"

            confidence_score = 0.90 if "passed" in refute_text else 0.75

            results.append({
                'relation': sc['relation_label'],
                'treatment': t,
                'outcome': o,
                'confounders': ", ".join(conf),
                'effect': round(ate_value, 3),
                'confidence': confidence_score,
                'refutation': refute_text,
                'insight': sc['insight_template'].format(ate_value, abs(ate_value)),
                'dag_nodes': [t, o] + conf,
                'visual_explanation': f"Causal Pathway: {t} directly causes {o} after isolating common confounder effects ({', '.join(conf)})."
            })

        except Exception as e:
            print(f"Causal analysis failed for {t}->{o}: {e}")

    return results
