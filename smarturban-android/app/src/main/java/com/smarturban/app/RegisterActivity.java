package com.smarturban.app;

import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etAddress, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppLocaleManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginLink = findViewById(R.id.tv_login_link);

        btnRegister.setOnClickListener(v -> handleRegistration());

        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void handleRegistration() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String pwd = etPassword.getText().toString().trim();
        String confirmPwd = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, R.string.field_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pwd.equals(confirmPwd)) {
            Toast.makeText(this, R.string.password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("fullName", name);
            json.put("email", email);
            json.put("phone", phone);
            json.put("address", address);
            json.put("password", pwd);

            btnRegister.setEnabled(false);

            HttpNetworkClient.sendJsonRequest(ApiConfig.REGISTER_URL, "POST", json.toString(), null, new HttpNetworkClient.ApiResponseCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    btnRegister.setEnabled(true);
                    if (statusCode == 200 || statusCode == 201) {
                        Toast.makeText(RegisterActivity.this, R.string.registration_success, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String msg = "Registration failed";
                        try {
                            if (responseBody != null) {
                                JSONObject err = new JSONObject(responseBody);
                                if (err.has("message")) msg = err.getString("message");
                            }
                        } catch (Exception ignored) {}
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    btnRegister.setEnabled(true);
                    String errDetail = e != null && e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Connection failed";
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + errDetail, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, R.string.field_required, Toast.LENGTH_SHORT).show();
        }
    }
}
