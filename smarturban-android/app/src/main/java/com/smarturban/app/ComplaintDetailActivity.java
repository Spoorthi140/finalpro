package com.smarturban.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class ComplaintDetailActivity extends AppCompatActivity {

    private TextView tvDetailId, tvDetailTitle, tvDetailStatus, tvDetailDesc, tvDetailLocation;
    private LinearLayout containerHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_detail);

        tvDetailId = findViewById(R.id.tv_detail_id);
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        tvDetailStatus = findViewById(R.id.tv_detail_status);
        tvDetailDesc = findViewById(R.id.tv_detail_desc);
        tvDetailLocation = findViewById(R.id.tv_detail_location);
        containerHistory = findViewById(R.id.container_history);

        long cmpId = getIntent().getLongExtra("complaint_id", -1);
        if (cmpId != -1) {
            loadComplaintDetails(cmpId);
            loadComplaintHistory(cmpId);
        }
    }

    private void loadComplaintDetails(long cmpId) {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        String url = ApiConfig.MY_COMPLAINTS_URL + "/" + cmpId;

        HttpNetworkClient.sendJsonRequest(url, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONObject obj = new JSONObject(responseBody);
                        tvDetailId.setText("#CMP-" + obj.getLong("id"));
                        tvDetailTitle.setText(obj.getString("title"));
                        tvDetailStatus.setText(obj.optString("status", "Submitted"));
                        tvDetailDesc.setText(obj.getString("description"));

                        String locName = obj.optString("locationName", "Captured Location");
                        double lat = obj.optDouble("latitude", 0.0);
                        double lng = obj.optDouble("longitude", 0.0);

                        tvDetailLocation.setText(locName + " (Lat: " + lat + ", Long: " + lng + ")");
                    } catch (Exception e) {
                        Toast.makeText(ComplaintDetailActivity.this, "Error parsing complaint details", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ComplaintDetailActivity.this, "Error loading details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComplaintHistory(long cmpId) {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        String url = ApiConfig.MY_COMPLAINTS_URL + "/" + cmpId + "/history";

        HttpNetworkClient.sendJsonRequest(url, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONArray array = new JSONArray(responseBody);
                        containerHistory.removeAllViews();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String newStatus = obj.optString("newStatus", "");
                            String changedBy = obj.optString("changedBy", "System");
                            String changedAt = obj.optString("changedAt", "");
                            String remarks = obj.optString("remarks", "");

                            if (changedAt.length() >= 16) changedAt = changedAt.substring(0, 16).replace("T", " ");

                            TextView historyItem = new TextView(ComplaintDetailActivity.this);
                            historyItem.setText("• [" + newStatus + "] by " + changedBy + " at " + changedAt + (remarks.isEmpty() ? "" : "\n  Remarks: " + remarks));
                            historyItem.setPadding(0, 8, 0, 8);
                            historyItem.setTextColor(0xFF475569);
                            containerHistory.addView(historyItem);
                        }
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onError(Exception e) {}
        });
    }
}
