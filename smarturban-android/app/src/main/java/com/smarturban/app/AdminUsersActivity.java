package com.smarturban.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    private ListView lvAdminUsers;

    static class UserItem {
        long id;
        String name;
        String email;
        boolean enabled;

        UserItem(long id, String name, String email, boolean enabled) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.enabled = enabled;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        lvAdminUsers = findViewById(R.id.lv_admin_users);
        loadUsersFromBackend();
    }

    private void loadUsersFromBackend() {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        HttpNetworkClient.sendJsonRequest(ApiConfig.ADMIN_USERS_URL, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONArray array = new JSONArray(responseBody);
                        List<UserItem> list = new ArrayList<>();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            long id = obj.getLong("id");
                            String name = obj.optString("fullName", "User");
                            String email = obj.optString("email", "");
                            boolean enabled = obj.optBoolean("enabled", true);

                            list.add(new UserItem(id, name, email, enabled));
                        }

                        displayUsers(list);
                    } catch (Exception e) {
                        Toast.makeText(AdminUsersActivity.this, "Error parsing users", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminUsersActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUsers(List<UserItem> list) {
        ArrayAdapter<UserItem> adapter = new ArrayAdapter<UserItem>(this, 0, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
                }
                UserItem item = getItem(position);

                TextView tvName = convertView.findViewById(R.id.tv_user_name);
                TextView tvEmail = convertView.findViewById(R.id.tv_user_email);
                Button btnToggle = convertView.findViewById(R.id.btn_user_toggle);
                Button btnDelete = convertView.findViewById(R.id.btn_user_delete);

                if (item != null) {
                    tvName.setText(item.name + (item.enabled ? "" : " (Disabled)"));
                    tvEmail.setText(item.email);

                    btnToggle.setText(item.enabled ? "Disable" : "Enable");

                    btnToggle.setOnClickListener(v -> toggleUser(item.id));
                    btnDelete.setOnClickListener(v -> deleteUser(item.id, item.email));
                }

                return convertView;
            }
        };

        lvAdminUsers.setAdapter(adapter);
    }

    private void toggleUser(long userId) {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        String url = ApiConfig.ADMIN_USERS_URL + "/" + userId + "/toggle";

        HttpNetworkClient.sendJsonRequest(url, "PUT", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200) {
                    Toast.makeText(AdminUsersActivity.this, "User status toggled", Toast.LENGTH_SHORT).show();
                    loadUsersFromBackend();
                } else {
                    Toast.makeText(AdminUsersActivity.this, "Cannot modify account", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    private void deleteUser(long userId, String email) {
        if ("admin@smarturban.com".equalsIgnoreCase(email)) {
            Toast.makeText(this, "Cannot delete primary admin account", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        String url = ApiConfig.ADMIN_USERS_URL + "/" + userId;

        HttpNetworkClient.sendJsonRequest(url, "DELETE", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200) {
                    Toast.makeText(AdminUsersActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    loadUsersFromBackend();
                } else {
                    Toast.makeText(AdminUsersActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {}
        });
    }
}
