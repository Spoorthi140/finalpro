# SmartBiz Enterprise
### AI-Powered Business Intelligence, Causal Decision Intelligence & Smart Inventory Management Platform

SmartBiz Enterprise is a modern business decision support system that combines Causal AI, machine learning forecasting, and computer vision to help organizations optimize their operations.

## Key Features
- **AI Business Strategy Simulator:** Test price and marketing changes before implementation.
- **Causal Decision Intelligence:** Understand *why* business outcomes occur using DoWhy.
- **Weighted Ensemble Forecasting:** Advanced predictions using Prophet, ARIMA, XGBoost, and Random Forest.
- **Smart Inventory Monitoring:** Camera-based object counting and stock-out risk assessment.
- **Executive Decision Support:** Health scores and risk alerts for business leaders.
- **Role-Based Access Control:** 5 distinct user roles (Super Admin to Viewer).

---

## 🛠 Setup Instructions

### 1. Prerequisites
- Python 3.9+
- MySQL Server (Optional, defaults to SQLite if not configured)
- Webcam (For Smart Inventory features)

### 2. Installation
Clone the repository and install dependencies:
```bash
pip install -r requirements.txt
```

### 3. Database Configuration
By default, the application uses **SQLite** for easy setup. To use **MySQL**:

1. Create a database named `smartbiz_db` in MySQL Workbench.
2. Set the environment variable `USE_MYSQL=1`.
3. (Optional) Customize the connection string in `smartbiz/app.py`:
   `mysql+pymysql://root:YOUR_PASSWORD@localhost/smartbiz_db`

### 4. Running the Application
```bash
python run.py
```
- Access the platform: `http://127.0.0.1:5000`
- Admin Login: `http://127.0.0.1:5000/admin/login`

### 🔑 Default Admin Credentials
- **Email:** `admin@smartbiz.com`
- **Password:** `admin123`
*(Note: These are initialized on first run or via `setup_admin.py`)*

---

## 📊 Modules
- **Module 1-2:** Home & Company Profile
- **Module 3:** Intelligent Data Hub (CSV/Excel Upload & Validation)
- **Module 4:** BI Dashboard (Interactive KPI tracking)
- **Module 5:** Causal Engine (DoWhy-powered impact analysis)
- **Module 6:** Strategy Simulator (Scenario testing)
- **Module 7:** AI Prediction (Multi-model forecasting)
- **Module 8:** Customer Intelligence (RFM & Segmentation)
- **Module 9:** Smart Inventory (OpenCV Camera processing)
- **Module 10-11:** AI Recommendations & Advanced Reporting (PDF/Excel)
- **Module 12:** Executive Support Center

## 🎨 UI Theme: "Midnight Modern"
- **Background:** Dark-mode glassmorphism.
- **Highlights:** Neon Blue and Cyber Red accents.
- **Framework:** Bootstrap 5 + Chart.js.

---
**Author:** SmartBiz Development Team
**Version:** 1.0.0 (Production-Ready)
