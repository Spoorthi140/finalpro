package com.smarturban.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotificationsActivity extends AppCompatActivity {

    private LinearLayout containerNotifications;
    private TextView tvEmptyNotifications;
    private Button btnMarkAllRead;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppLocaleManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        containerNotifications = findViewById(R.id.container_notifications);
        tvEmptyNotifications = findViewById(R.id.tv_empty_notifications);
        btnMarkAllRead = findViewById(R.id.btn_mark_all_read);
        bottomNavigationView = findViewById(R.id.bottom_navigation_notifications);

        setupBottomNavigation();

        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());

        loadNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_notifications);
        }
        loadNotifications();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_notifications);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_complaints) {
                startActivity(new Intent(this, MyComplaintsActivity.class));
                return true;
            } else if (itemId == R.id.nav_notifications) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadNotifications() {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        HttpNetworkClient.sendJsonRequest(ApiConfig.NOTIFICATIONS_URL, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONArray array = new JSONArray(responseBody);
                        containerNotifications.removeAllViews();

                        if (array.length() == 0) {
                            tvEmptyNotifications.setVisibility(View.VISIBLE);
                        } else {
                            tvEmptyNotifications.setVisibility(View.GONE);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                renderNotificationItem(obj);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("NotificationsActivity", "Error parsing notifications", e);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                android.util.Log.e("NotificationsActivity", "Failed to fetch notifications", e);
            }
        });
    }

    private void renderNotificationItem(JSONObject obj) {
        try {
            View cardView = LayoutInflater.from(this).inflate(R.layout.item_notification, containerNotifications, false);

            TextView tvTitle = cardView.findViewById(R.id.tv_notif_title);
            TextView tvMessage = cardView.findViewById(R.id.tv_notif_message);
            TextView tvDate = cardView.findViewById(R.id.tv_notif_date);
            TextView tvUnreadTag = cardView.findViewById(R.id.tv_notif_unread_tag);
            MaterialCardView card = (MaterialCardView) cardView;

            long notifId = obj.getLong("id");
            long complaintId = obj.optLong("complaintId", -1);
            String title = obj.getString("title");
            String message = obj.getString("message");
            boolean isRead = obj.optBoolean("isRead", false);
            String date = obj.optString("createdAt", "");
            if (date.length() >= 16) date = date.substring(0, 16).replace("T", " ");

            tvTitle.setText(title);
            tvMessage.setText(message);
            tvDate.setText(date);

            if (!isRead) {
                tvUnreadTag.setVisibility(View.VISIBLE);
                card.setCardBackgroundColor(Color.parseColor("#F1F5F9"));
            } else {
                tvUnreadTag.setVisibility(View.GONE);
                card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            }

            cardView.setOnClickListener(v -> {
                markAsRead(notifId);
                if (complaintId != -1) {
                    Intent intent = new Intent(NotificationsActivity.this, ComplaintDetailActivity.class);
                    intent.putExtra("complaint_id", complaintId);
                    startActivity(intent);
                }
            });

            containerNotifications.addView(cardView);
        } catch (Exception e) {
            android.util.Log.e("NotificationsActivity", "Error rendering notification item", e);
        }
    }

    private void markAsRead(long notifId) {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        String url = ApiConfig.NOTIFICATIONS_URL + "/" + notifId + "/read";

        HttpNetworkClient.sendJsonRequest(url, "PUT", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                loadNotifications();
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    private void markAllAsRead() {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        String url = ApiConfig.NOTIFICATIONS_URL + "/read-all";

        HttpNetworkClient.sendJsonRequest(url, "PUT", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                Toast.makeText(NotificationsActivity.this, "Notifications marked as read", Toast.LENGTH_SHORT).show();
                loadNotifications();
            }

            @Override
            public void onError(Exception e) {}
        });
    }
}
