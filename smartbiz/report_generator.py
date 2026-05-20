from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas
from reportlab.lib import colors
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet
import io

def generate_pdf_report(products, total_sales, total_profit):
    buffer = io.BytesIO()
    doc = SimpleDocTemplate(buffer, pagesize=letter)
    elements = []

    styles = getSampleStyleSheet()
    title = Paragraph("SmartBiz Business Optimization Report", styles['Title'])
    elements.append(title)
    elements.append(Spacer(1, 12))

    # Summary
    summary = f"Total Sales: {total_sales} | Total Profit: ${total_profit:.2f}"
    elements.append(Paragraph(summary, styles['Heading2']))
    elements.append(Spacer(1, 20))

    # Product Table
    data = [['Product', 'Price', 'Stock', 'Sales', 'Profit']]
    for p in products:
        data.append([p.product_name, f"${p.price:.2f}", p.stock_quantity, p.sales, f"${p.profit:.2f}"])

    t = Table(data)
    t.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.navy),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
        ('BACKGROUND', (0, 1), (-1, -1), colors.whitesmoke),
        ('GRID', (0,0), (-1,-1), 1, colors.black)
    ]))
    elements.append(t)

    doc.build(elements)
    buffer.seek(0)
    return buffer
