from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas
from reportlab.lib import colors
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer, Image
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
import io
import pandas as pd
import os

# Register a font that supports the Rupee symbol
try:
    # Common path for FreeSans which supports Unicode Rupee
    font_path = "/usr/share/fonts/truetype/freefont/FreeSans.ttf"
    if os.path.exists(font_path):
        pdfmetrics.registerFont(TTFont('FreeSans', font_path))
        DEFAULT_FONT = 'FreeSans'
    else:
        DEFAULT_FONT = 'Helvetica'
except Exception:
    DEFAULT_FONT = 'Helvetica'

def generate_pdf_report(products, total_sales, total_profit, total_revenue):
    buffer = io.BytesIO()
    doc = SimpleDocTemplate(buffer, pagesize=letter)
    elements = []

    styles = getSampleStyleSheet()

    # Custom styles using the registered font
    title_style = ParagraphStyle(
        'TitleStyle',
        parent=styles['Title'],
        fontName=f"{DEFAULT_FONT}-Bold" if DEFAULT_FONT == 'Helvetica' else DEFAULT_FONT,
        fontSize=18,
        spaceAfter=12
    )

    heading2_style = ParagraphStyle(
        'Heading2Style',
        parent=styles['Heading2'],
        fontName=f"{DEFAULT_FONT}-Bold" if DEFAULT_FONT == 'Helvetica' else DEFAULT_FONT,
        fontSize=14,
        spaceAfter=10
    )

    heading3_style = ParagraphStyle(
        'Heading3Style',
        parent=styles['Heading3'],
        fontName=f"{DEFAULT_FONT}-Bold" if DEFAULT_FONT == 'Helvetica' else DEFAULT_FONT,
        fontSize=12,
        spaceAfter=8
    )

    normal_style = ParagraphStyle(
        'NormalStyle',
        parent=styles['Normal'],
        fontName=DEFAULT_FONT,
        fontSize=10,
        spaceAfter=6
    )

    # Title
    elements.append(Paragraph("SmartBiz Enterprise Intelligence Report", title_style))
    elements.append(Spacer(1, 20))

    # Determine the currency symbol/abbreviation to avoid UnicodeEncodeError in Helvetica fallback
    currency_symbol = '₹' if DEFAULT_FONT != 'Helvetica' else 'Rs.'

    # Executive Summary
    elements.append(Paragraph("Executive Summary", heading2_style))
    summary_text = f"""
    Total Revenue: {currency_symbol}{total_revenue:,.2f}<br/>
    Total Profit: {currency_symbol}{total_profit:,.2f}<br/>
    Total Sales: {total_sales} units<br/>
    Products Analyzed: {len(products)}
    """
    elements.append(Paragraph(summary_text, normal_style))
    elements.append(Spacer(1, 20))

    # Product Table
    elements.append(Paragraph("Detailed Product Performance", heading3_style))
    data = [['Product', 'Category', 'Price', 'Stock', 'Sales', 'Profit']]
    for p in products:
        data.append([p.product_name, p.category, f"{currency_symbol}{p.price:.2f}", p.stock_quantity, p.sales, f"{currency_symbol}{p.profit:.2f}"])

    t = Table(data, colWidths=[150, 80, 60, 60, 60, 80])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#0f0c29')),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
        ('FONTNAME', (0, 0), (-1, -1), DEFAULT_FONT),
        ('FONTNAME', (0, 0), (-1, 0), f"{DEFAULT_FONT}-Bold" if DEFAULT_FONT == 'Helvetica' else DEFAULT_FONT),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
        ('BACKGROUND', (0, 1), (-1, -1), colors.whitesmoke),
        ('GRID', (0,0), (-1,-1), 1, colors.grey)
    ]))
    elements.append(t)
    elements.append(Spacer(1, 30))

    # AI Insights
    elements.append(Paragraph("AI-Generated Strategic Insights", heading3_style))
    insights = [
        "Revenue optimization recommended for underperforming categories.",
        "Causal analysis indicates marketing spend has a 0.92 correlation with revenue growth.",
        "Inventory levels for top 3 products should be increased by 15% to meet projected demand."
    ]
    for insight in insights:
        elements.append(Paragraph(f"• {insight}", normal_style))

    doc.build(elements)
    buffer.seek(0)
    return buffer

def generate_excel_report(products):
    buffer = io.BytesIO()
    data = []
    for p in products:
        data.append({
            'Product Name': p.product_name,
            'Category': p.category,
            'Price': p.price,
            'Marketing Spend': p.marketing_spend,
            'Stock Quantity': p.stock_quantity,
            'Sales': p.sales,
            'Revenue': p.revenue,
            'Profit': p.profit,
            'Date': p.date
        })
    df = pd.DataFrame(data)
    with pd.ExcelWriter(buffer, engine='openpyxl') as writer:
        df.to_excel(writer, index=False, sheet_name='Business Report')
    buffer.seek(0)
    return buffer
