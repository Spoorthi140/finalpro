package com.smarturban.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvHelloCitizen, tvCountTotal, tvCountPending, tvCountInProgress, tvCountResolved, tvEmptyRecent;
    private ImageView btnLogoutIcon;
    private MaterialCardView cardReportComplaint;
    private LinearLayout containerRecentComplaints;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppLocaleManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvHelloCitizen = findViewById(R.id.tv_hello_citizen);
        tvCountTotal = findViewById(R.id.tv_count_total);
        tvCountPending = findViewById(R.id.tv_count_pending);
        tvCountInProgress = findViewById(R.id.tv_count_in_progress);
        tvCountResolved = findViewById(R.id.tv_count_resolved);
        tvEmptyRecent = findViewById(R.id.tv_empty_recent_complaints);

        btnLogoutIcon = findViewById(R.id.btn_logout_icon);
        cardReportComplaint = findViewById(R.id.card_report_complaint_action);
        containerRecentComplaints = findViewById(R.id.container_recent_complaints);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        setupBottomNavigation();

        cardReportComplaint.setOnClickListener(v -> startActivity(new Intent(this, SubmitComplaintActivity.class)));

        btnLogoutIcon.setOnClickListener(v -> performLogout());

        loadDashboardData();
        loadUnreadNotificationsCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
        loadDashboardData();
        loadUnreadNotificationsCount();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_complaints) {
                startActivity(new Intent(this, MyComplaintsActivity.class));
                return true;
            } else if (itemId == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationsActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadUnreadNotificationsCount() {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        HttpNetworkClient.sendJsonRequest(ApiConfig.NOTIFICATIONS_URL + "/unread-count", "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        int count = json.optInt("unreadCount", 0);
                        if (count > 0) {
                            bottomNavigationView.getOrCreateBadge(R.id.nav_notifications).setNumber(count);
                        } else {
                            bottomNavigationView.removeBadge(R.id.nav_notifications);
                        }
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    private void loadDashboardData() {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        HttpNetworkClient.sendJsonRequest(ApiConfig.PROFILE_URL, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String name = json.optString("fullName", getString(R.string.welcome_citizen));
                        tvHelloCitizen.setText("Hello, " + name + " 👋");
                    } catch (Exception ignored) {}
                }
            }
            @Override public void onError(Exception e) {}
        });

        HttpNetworkClient.sendJsonRequest(ApiConfig.MY_COMPLAINTS_URL, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONArray array = new JSONArray(responseBody);
                        int total = array.length();
                        int pending = 0;
                        int inProgress = 0;
                        int resolved = 0;

                        containerRecentComplaints.removeAllViews();

                        for (int i = 0; i < total; i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String status = obj.optString("status", "Submitted");

                            if ("Submitted".equalsIgnoreCase(status) || "Under Review".equalsIgnoreCase(status)) {
                                pending++;
                            } else if ("In Progress".equalsIgnoreCase(status) || "Assigned".equalsIgnoreCase(status)) {
                                inProgress++;
                            } else if ("Resolved".equalsIgnoreCase(status)) {
                                resolved++;
                            }

                            if (i < 3) {
                                renderRecentComplaintCard(obj);
                            }
                        }

                        tvCountTotal.setText(String.valueOf(total));
                        tvCountPending.setText(String.valueOf(pending));
                        tvCountInProgress.setText(String.valueOf(inProgress));
                        tvCountResolved.setText(String.valueOf(resolved));

                        if (total == 0) {
                            tvEmptyRecent.setVisibility(View.VISIBLE);
                        } else {
                            tvEmptyRecent.setVisibility(View.GONE);
                        }

                    } catch (Exception ignored) {}
                }
            }
            @Override public void onError(Exception e) {}
        });
    }

    private void renderRecentComplaintCard(JSONObject obj) {
        try {
            View cardView = LayoutInflater.from(this).inflate(R.layout.item_complaint, containerRecentComplaints, false);

            TextView tvId = cardView.findViewById(R.id.tv_cmp_id);
            TextView tvTitle = cardView.findViewById(R.id.tv_cmp_title);
            TextView tvCat = cardView.findViewById(R.id.tv_cmp_category);
            TextView tvStatus = cardView.findViewById(R.id.tv_cmp_status);
            TextView tvDate = cardView.findViewById(R.id.tv_cmp_date);

            long cmpId = obj.getLong("id");
            String title = obj.getString("title");
            String status = obj.optString("status", "Submitted");
            String date = obj.optString("createdAt", "");
            if (date.length() >= 10) date = date.substring(0, 10);

            String category = "General";
            if (obj.has("category") && !obj.isNull("category")) {
                category = obj.getJSONObject("category").optString("name", "General");
            }

            tvId.setText("#CMP-" + cmpId);
            tvTitle.setText(title);
            tvCat.setText(getString(R.string.category) + ": " + category);
            tvStatus.setText(status);
            tvDate.setText("Date: " + date);

            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, ComplaintDetailActivity.class);
                intent.putExtra("complaint_id", cmpId);
                startActivity(intent);
            });

            containerRecentComplaints.addView(cardView);
        } catch (Exception ignored) {}
    }

    private void performLogout() {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
