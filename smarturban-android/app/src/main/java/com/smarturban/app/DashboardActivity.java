package com.smarturban.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcomeUser, tvUserEmail;
    private Button btnSubmitComplaint, btnMyComplaints, btnProfile, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvWelcomeUser = findViewById(R.id.tv_welcome_user);
        tvUserEmail = findViewById(R.id.tv_user_email);
        btnSubmitComplaint = findViewById(R.id.btn_submit_complaint);
        btnMyComplaints = findViewById(R.id.btn_my_complaints);
        btnProfile = findViewById(R.id.btn_profile);
        btnLogout = findViewById(R.id.btn_logout);

        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String email = pref.getString("email", "citizen@smarturban.gov");

        tvUserEmail.setText(email);
        tvWelcomeUser.setText("Welcome Citizen");

        btnSubmitComplaint.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, SubmitComplaintActivity.class));
        });

        btnMyComplaints.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, MyComplaintsActivity.class));
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
