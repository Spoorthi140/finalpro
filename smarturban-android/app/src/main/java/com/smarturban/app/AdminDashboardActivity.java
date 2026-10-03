package com.smarturban.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvCitizens, tvComplaints, tvPending, tvInProgress, tvResolved, tvRejected;
    private Button btnComplaints, btnUsers, btnCategories, btnDepartments, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvCitizens = findViewById(R.id.tv_stat_citizens);
        tvComplaints = findViewById(R.id.tv_stat_complaints);
        tvPending = findViewById(R.id.tv_stat_pending);
        tvInProgress = findViewById(R.id.tv_stat_in_progress);
        tvResolved = findViewById(R.id.tv_stat_resolved);
        tvRejected = findViewById(R.id.tv_stat_rejected);

        btnComplaints = findViewById(R.id.btn_manage_complaints);
        btnUsers = findViewById(R.id.btn_manage_users);
        btnCategories = findViewById(R.id.btn_manage_categories);
        btnDepartments = findViewById(R.id.btn_manage_departments);
        btnLogout = findViewById(R.id.btn_admin_logout);

        loadDashboardStats();

        btnComplaints.setOnClickListener(v -> startActivity(new Intent(this, AdminComplaintsActivity.class)));
        btnUsers.setOnClickListener(v -> startActivity(new Intent(this, AdminUsersActivity.class)));
        btnCategories.setOnClickListener(v -> startActivity(new Intent(this, AdminCategoriesActivity.class)));
        btnDepartments.setOnClickListener(v -> startActivity(new Intent(this, AdminDepartmentsActivity.class)));

        btnLogout.setOnClickListener(v -> {
            SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadDashboardStats() {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        HttpNetworkClient.sendJsonRequest(ApiConfig.ADMIN_STATS_URL, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONObject obj = new JSONObject(responseBody);
                        tvCitizens.setText(String.valueOf(obj.optLong("totalUsers", 0)));
                        tvComplaints.setText(String.valueOf(obj.optLong("totalComplaints", 0)));
                        tvPending.setText(String.valueOf(obj.optLong("pendingComplaints", 0)));
                        tvInProgress.setText(String.valueOf(obj.optLong("inProgressComplaints", 0)));
                        tvResolved.setText(String.valueOf(obj.optLong("resolvedComplaints", 0)));
                        tvRejected.setText(String.valueOf(obj.optLong("rejectedComplaints", 0)));
                    } catch (Exception e) {
                        Toast.makeText(AdminDashboardActivity.this, "Error parsing stats", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Failed to load admin stats (" + statusCode + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminDashboardActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
