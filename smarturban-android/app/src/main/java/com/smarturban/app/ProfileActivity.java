package com.smarturban.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

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

        loadProfileFromBackend();

        btnSaveProfile.setOnClickListener(v -> saveProfileToBackend());
    }

    private void loadProfileFromBackend() {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        HttpNetworkClient.sendJsonRequest(ApiConfig.PROFILE_URL, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        etProfName.setText(json.optString("fullName", ""));
                        etProfEmail.setText(json.optString("email", ""));
                        etProfPhone.setText(json.optString("phone", ""));
                        etProfAddress.setText(json.optString("address", ""));
                    } catch (Exception e) {
                        Toast.makeText(ProfileActivity.this, "Error parsing profile data", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ProfileActivity.this, "Network error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileToBackend() {
        String fullName = etProfName.getText().toString().trim();
        String phone = etProfPhone.getText().toString().trim();
        String address = etProfAddress.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, R.string.field_required, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("fullName", fullName);
            json.put("phone", phone);
            json.put("address", address);

            SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
            String jwtToken = pref.getString("token", "");

            btnSaveProfile.setEnabled(false);

            HttpNetworkClient.sendJsonRequest(ApiConfig.PROFILE_URL, "PUT", json.toString(), jwtToken, new HttpNetworkClient.ApiResponseCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    btnSaveProfile.setEnabled(true);
                    if (statusCode == 200) {
                        Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Network error updating profile", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error preparing request", Toast.LENGTH_SHORT).show();
        }
    }
}
