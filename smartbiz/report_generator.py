from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas
from reportlab.lib import colors
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer, Image
from reportlab.lib.styles import getSampleStyleSheet
import io

def generate_pdf_report(products, total_sales, total_profit, causal_insights=None):
    buffer = io.BytesIO()
    doc = SimpleDocTemplate(buffer, pagesize=letter)
    elements = []

    styles = getSampleStyleSheet()

    # Title
    elements.append(Paragraph("SmartBiz Business Strategy Optimization Report", styles['Title']))
    elements.append(Spacer(1, 20))

    # Executive Summary
    elements.append(Paragraph("Executive Summary", styles['Heading2']))
    summary_text = f"Total Sales: {total_sales} units<br/>Total Profit: ${total_profit:.2f}<br/>Total Products Monitored: {len(products)}"
    elements.append(Paragraph(summary_text, styles['Normal']))
    elements.append(Spacer(1, 20))

    # Inventory & Performance Table
    elements.append(Paragraph("Product Performance & Inventory Levels", styles['Heading3']))
    data = [['Product Name', 'Price', 'Stock', 'Sales', 'Profit']]
    for p in products:
        data.append([p.product_name, f"${p.price:.2f}", p.stock_quantity, p.sales, f"${p.profit:.2f}"])

    t = Table(data, colWidths=[150, 70, 70, 70, 100])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.navy),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
        ('BACKGROUND', (0, 1), (-1, -1), colors.whitesmoke),
        ('GRID', (0,0), (-1,-1), 1, colors.grey)
    ]))
    elements.append(t)
    elements.append(Spacer(1, 30))

    # Causal AI Insights Section
    elements.append(Paragraph("AI-Driven Causal Insights", styles['Heading3']))
    insights = causal_insights if causal_insights else [
        "Increasing marketing budget generally improves sales volume.",
        "Price sensitivity detected: Higher pricing negatively impacts unit demand.",
        "Stock availability is a primary driver for profit realization."
    ]
    for insight in insights:
        elements.append(Paragraph(f"• {insight}", styles['Normal']))

    doc.build(elements)
    buffer.seek(0)
    return buffer
