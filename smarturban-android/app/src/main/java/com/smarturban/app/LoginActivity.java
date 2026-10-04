package com.smarturban.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppLocaleManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegisterLink = findViewById(R.id.tv_register_link);

        btnLogin.setOnClickListener(v -> handleLogin());

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.field_required, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", password);

            btnLogin.setEnabled(false);

            HttpNetworkClient.sendJsonRequest(ApiConfig.LOGIN_URL, "POST", json.toString(), null, new HttpNetworkClient.ApiResponseCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    btnLogin.setEnabled(true);
                    if (statusCode == 200 && responseBody != null) {
                        try {
                            JSONObject responseJson = new JSONObject(responseBody);
                            String token = responseJson.optString("token");
                            String userEmail = responseJson.optString("email");
                            String fullName = responseJson.optString("fullName");
                            String role = responseJson.optString("role", "ROLE_CITIZEN");
                            long userId = responseJson.optLong("id");

                            if ("ROLE_ADMIN".equalsIgnoreCase(role)) {
                                Toast.makeText(LoginActivity.this, "Administrator accounts must use the Web Admin Panel.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("token", token);
                            editor.putString("email", userEmail);
                            editor.putString("fullName", fullName);
                            editor.putString("role", role);
                            editor.putLong("userId", userId);
                            editor.apply();

                            Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
