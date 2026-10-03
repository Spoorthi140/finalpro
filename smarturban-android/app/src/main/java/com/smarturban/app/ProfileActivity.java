package com.smarturban.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private EditText etProfName, etProfEmail, etProfPhone, etProfAddress;
    private Button btnSaveProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etProfName = findViewById(R.id.et_prof_name);
        etProfEmail = findViewById(R.id.et_prof_email);
        etProfPhone = findViewById(R.id.et_prof_phone);
        etProfAddress = findViewById(R.id.et_prof_address);
        btnSaveProfile = findViewById(R.id.btn_save_profile);

        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String email = pref.getString("email", "citizen@smarturban.gov");

        etProfEmail.setText(email);
        etProfName.setText("John Citizen");
        etProfPhone.setText("9876543210");
        etProfAddress.setText("123 City Center, MG Road");

        btnSaveProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
