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
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

public class MyComplaintsActivity extends AppCompatActivity {

    private Button chipAll, chipPending, chipInProgress, chipResolved;
    private LinearLayout containerMyComplaints;
    private TextView tvEmptyMyComplaints;
    private BottomNavigationView bottomNavigationView;

    private String currentFilter = "ALL";
    private JSONArray allComplaintsArray = new JSONArray();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppLocaleManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_complaints);

        chipAll = findViewById(R.id.chip_filter_all);
        chipPending = findViewById(R.id.chip_filter_pending);
        chipInProgress = findViewById(R.id.chip_filter_in_progress);
        chipResolved = findViewById(R.id.chip_filter_resolved);

        containerMyComplaints = findViewById(R.id.container_my_complaints);
        tvEmptyMyComplaints = findViewById(R.id.tv_empty_my_complaints);
        bottomNavigationView = findViewById(R.id.bottom_navigation_my_complaints);

        setupBottomNavigation();

        chipAll.setOnClickListener(v -> setFilter("ALL"));
        chipPending.setOnClickListener(v -> setFilter("PENDING"));
        chipInProgress.setOnClickListener(v -> setFilter("IN_PROGRESS"));
        chipResolved.setOnClickListener(v -> setFilter("RESOLVED"));

        loadComplaintsFromBackend();
        loadUnreadNotificationsCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_complaints);
        }
        loadComplaintsFromBackend();
        loadUnreadNotificationsCount();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_complaints);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_complaints) {
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

    private void setFilter(String filter) {
        currentFilter = filter;
        updateChipStyles();
        renderComplaintsList();
    }

    private void updateChipStyles() {
        resetChip(chipAll);
        resetChip(chipPending);
        resetChip(chipInProgress);
        resetChip(chipResolved);

        if ("ALL".equalsIgnoreCase(currentFilter)) {
            setActiveChip(chipAll);
        } else if ("PENDING".equalsIgnoreCase(currentFilter)) {
            setActiveChip(chipPending);
        } else if ("IN_PROGRESS".equalsIgnoreCase(currentFilter)) {
            setActiveChip(chipInProgress);
        } else if ("RESOLVED".equalsIgnoreCase(currentFilter)) {
            setActiveChip(chipResolved);
        }
    }

    private void resetChip(Button btn) {
        btn.setBackgroundColor(Color.parseColor("#334155"));
        btn.setTextColor(Color.parseColor("#94A3B8"));
    }

    private void setActiveChip(Button btn) {
        btn.setBackgroundColor(Color.parseColor("#0284C7"));
        btn.setTextColor(Color.parseColor("#FFFFFF"));
    }

    private void loadComplaintsFromBackend() {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        HttpNetworkClient.sendJsonRequest(ApiConfig.MY_COMPLAINTS_URL, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        allComplaintsArray = new JSONArray(responseBody);
                        renderComplaintsList();
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    private void renderComplaintsList() {
        containerMyComplaints.removeAllViews();
        int count = 0;

        try {
            for (int i = 0; i < allComplaintsArray.length(); i++) {
                JSONObject obj = allComplaintsArray.getJSONObject(i);
                String status = obj.optString("status", "Submitted");

                boolean matches = false;
                if ("ALL".equalsIgnoreCase(currentFilter)) {
                    matches = true;
                } else if ("PENDING".equalsIgnoreCase(currentFilter) && ("Submitted".equalsIgnoreCase(status) || "Under Review".equalsIgnoreCase(status))) {
                    matches = true;
                } else if ("IN_PROGRESS".equalsIgnoreCase(currentFilter) && ("In Progress".equalsIgnoreCase(status) || "Assigned".equalsIgnoreCase(status))) {
                    matches = true;
                } else if ("RESOLVED".equalsIgnoreCase(currentFilter) && "Resolved".equalsIgnoreCase(status)) {
                    matches = true;
                }

                if (matches) {
                    count++;
                    renderComplaintItemCard(obj);
                }
            }
        } catch (Exception ignored) {}

        if (count == 0) {
            tvEmptyMyComplaints.setVisibility(View.VISIBLE);
        } else {
            tvEmptyMyComplaints.setVisibility(View.GONE);
        }
    }

    private void renderComplaintItemCard(JSONObject obj) {
        try {
            View cardView = LayoutInflater.from(this).inflate(R.layout.item_complaint, containerMyComplaints, false);

            TextView tvId = cardView.findViewById(R.id.tv_cmp_id);
            TextView tvTitle = cardView.findViewById(R.id.tv_cmp_title);
            TextView tvCat = cardView.findViewById(R.id.tv_cmp_category);
            TextView tvLocation = cardView.findViewById(R.id.tv_cmp_location);
            TextView tvStatus = cardView.findViewById(R.id.tv_cmp_status);
            TextView tvDate = cardView.findViewById(R.id.tv_cmp_date);

            long cmpId = obj.getLong("id");
            String title = obj.getString("title");
            String status = obj.optString("status", "Submitted");
            String locName = obj.optString("locationName", "Location unavailable");
            if (locName == null || locName.trim().isEmpty() || "null".equalsIgnoreCase(locName)) {
                locName = "Location unavailable";
            }
            String date = obj.optString("createdAt", "");
            if (date.length() >= 10) date = date.substring(0, 10);

            String category = "General";
            if (obj.has("category") && !obj.isNull("category")) {
                category = obj.getJSONObject("category").optString("name", "General");
            }

            tvId.setText("#CMP-" + cmpId);
            tvTitle.setText(title);
            tvCat.setText(getString(R.string.category) + ": " + category);
            tvLocation.setText("📍 " + locName);
            tvStatus.setText(status);
            tvDate.setText("📅 Date: " + date);

            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(MyComplaintsActivity.this, ComplaintDetailActivity.class);
                intent.putExtra("complaint_id", cmpId);
                startActivity(intent);
            });

            containerMyComplaints.addView(cardView);
        } catch (Exception ignored) {}
    }
}
