# SmartBiz Enterprise
AI-Powered Business Intelligence, Causal Decision Intelligence & Smart Inventory Management Platform

## Overview
SmartBiz Enterprise is a modern enterprise-level business intelligence platform that combines Causal AI, Machine Learning forecasting, and Computer Vision to help organizations make data-driven decisions.

### Key Features
- **Causal Decision Intelligence**: Understand "why" business outcomes occur using DoWhy-powered causal inference.
- **AI Forecasting & Prediction**: Predict sales, revenue, and profit trends using XGBoost, Prophet, and Random Forest.
- **Smart Strategy Simulator**: Test business scenarios (price changes, marketing spend) before implementation.
- **Computer Vision Inventory**: Real-time shelf monitoring and quantity estimation using OpenCV.
- **Role-Based Access Control**: Secure access for Super Admins, Managers, Analysts, and Viewers.
- **Advanced Reporting**: Automated PDF and Excel reports for sales and inventory.

## Technology Stack
- **Frontend**: HTML5, CSS3, Bootstrap 5, Chart.js
- **Backend**: Python Flask
- **Database**: MySQL (via SQLAlchemy & PyMySQL)
- **AI/ML**: Scikit-Learn, XGBoost, Prophet, DoWhy
- **Computer Vision**: OpenCV
- **Reporting**: ReportLab, OpenPyXL

## Setup Instructions

### Prerequisites
- Python 3.8+
- MySQL Server

### Installation
1. Clone the repository.
2. Create a virtual environment:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Set up the MySQL database:
   - Create a database named `smartbiz_db`.
   - Update the `SQLALCHEMY_DATABASE_URI` in `app.py` if necessary.

### Running the Application
```bash
python app.py
```
The application will be available at `http://localhost:5000`.

### Administrative Access
- **Admin Portal**: `http://localhost:5000/admin/login`
- **Default Logic**: The first user to register is automatically assigned the `Super Admin` role.

## Folder Structure
- `smartbiz/`: Main application package.
  - `static/`: CSS, JS, and image assets.
  - `templates/`: HTML templates (organized by user/admin).
  - `models.py`: SQLAlchemy database models.
  - `user_routes.py`: Customer-facing logic and analytics.
  - `admin_routes.py`: Administrative management and CRUD.
  - `causal_analysis.py`: Causal AI implementation.
  - `prediction.py`: ML forecasting and prediction.
  - `inventory_monitor.py`: OpenCV object detection.
  - `report_generator.py`: PDF and Excel report generation.

## License
MIT License
