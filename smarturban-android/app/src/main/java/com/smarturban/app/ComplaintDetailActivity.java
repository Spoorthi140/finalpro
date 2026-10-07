package com.smarturban.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class ComplaintDetailActivity extends AppCompatActivity {

    private TextView tvDetailId, tvDetailTitle, tvDetailStatus, tvDetailDesc, tvDetailLocation, tvSubmittedFeedback;
    private LinearLayout containerHistory, layoutFeedbackSection;
    private RatingBar ratingBar;
    private EditText etFeedbackComments;
    private Button btnSubmitFeedback;
    private MapView mapView;

    private long complaintId = -1;
    private Double latitude = null;
    private Double longitude = null;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppLocaleManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));

        setContentView(R.layout.activity_complaint_detail);

        tvDetailId = findViewById(R.id.tv_detail_id);
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        tvDetailStatus = findViewById(R.id.tv_detail_status);
        tvDetailDesc = findViewById(R.id.tv_detail_desc);
        tvDetailLocation = findViewById(R.id.tv_detail_location);
        containerHistory = findViewById(R.id.container_history);

        layoutFeedbackSection = findViewById(R.id.layout_feedback_section);
        ratingBar = findViewById(R.id.rating_bar);
        etFeedbackComments = findViewById(R.id.et_feedback_comments);
        btnSubmitFeedback = findViewById(R.id.btn_submit_feedback);
        tvSubmittedFeedback = findViewById(R.id.tv_submitted_feedback);

        mapView = findViewById(R.id.map_view);

        complaintId = getIntent().getLongExtra("complaint_id", -1);
        if (complaintId != -1) {
            loadComplaintDetails(complaintId);
            loadComplaintHistory(complaintId);
            checkFeedback(complaintId);
        }

        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
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
                        tvDetailId.setText(getString(R.string.cmp_id_fmt, obj.getLong("id")));
                        tvDetailTitle.setText(obj.getString("title"));
                        String status = obj.optString("status", "Submitted");
                        tvDetailStatus.setText(status);
                        tvDetailDesc.setText(obj.getString("description"));

                        String locName = obj.optString("locationName", getString(R.string.location_unavailable));
                        if (locName == null || locName.trim().isEmpty() || "null".equalsIgnoreCase(locName)) {
                            locName = getString(R.string.location_unavailable);
                        }

                        if (obj.has("latitude") && !obj.isNull("latitude") && obj.has("longitude") && !obj.isNull("longitude")) {
                            latitude = obj.getDouble("latitude");
                            longitude = obj.getDouble("longitude");
                            tvDetailLocation.setText(locName + " (" + String.format("%.4f", latitude) + ", " + String.format("%.4f", longitude) + ")");

                            setupOpenStreetMap(latitude, longitude, obj.getString("title"));
                        } else {
                            tvDetailLocation.setText(locName);
                            mapView.setVisibility(View.GONE);
                        }

                        if ("Resolved".equalsIgnoreCase(status)) {
                            layoutFeedbackSection.setVisibility(View.VISIBLE);
                        } else {
                            layoutFeedbackSection.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        android.util.Log.e("ComplaintDetailActivity", "Error parsing complaint details", e);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                android.util.Log.e("ComplaintDetailActivity", "Failed to fetch complaint details", e);
            }
        });
    }

    private void setupOpenStreetMap(double lat, double lng, String title) {
        if (mapView != null) {
            mapView.setVisibility(View.VISIBLE);
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);

            GeoPoint startPoint = new GeoPoint(lat, lng);
            mapView.getController().setZoom(16.0);
            mapView.getController().setCenter(startPoint);

            Marker marker = new Marker(mapView);
            marker.setPosition(startPoint);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(title);
            mapView.getOverlays().add(marker);
            mapView.invalidate();
        }
    }

    private void checkFeedback(long cmpId) {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        String url = ApiConfig.MY_COMPLAINTS_URL + "/" + cmpId + "/feedback";

        HttpNetworkClient.sendJsonRequest(url, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONObject obj = new JSONObject(responseBody);
                        int rating = obj.optInt("rating", 5);
                        String comments = obj.optString("comments", "");

                        ratingBar.setRating(rating);
                        ratingBar.setIsIndicator(true);
                        etFeedbackComments.setVisibility(View.GONE);
                        btnSubmitFeedback.setVisibility(View.GONE);

                        tvSubmittedFeedback.setVisibility(View.VISIBLE);
                        tvSubmittedFeedback.setText(getString(R.string.rating_stars_fmt, rating, comments.isEmpty() ? getString(R.string.none) : comments));
                    } catch (Exception e) {
                        android.util.Log.e("ComplaintDetailActivity", "Error parsing feedback", e);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                android.util.Log.e("ComplaintDetailActivity", "Failed to fetch feedback", e);
            }
        });
    }

    private void submitFeedback() {
        if (complaintId == -1) return;

        int rating = (int) ratingBar.getRating();
        String comments = etFeedbackComments.getText().toString().trim();

        try {
            JSONObject json = new JSONObject();
            json.put("rating", rating);
            json.put("comments", comments);

            SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
            String jwtToken = pref.getString("token", "");

            String url = ApiConfig.MY_COMPLAINTS_URL + "/" + complaintId + "/feedback";

            btnSubmitFeedback.setEnabled(false);

            HttpNetworkClient.sendJsonRequest(url, "POST", json.toString(), jwtToken, new HttpNetworkClient.ApiResponseCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    btnSubmitFeedback.setEnabled(true);
                    if (statusCode == 200) {
                        Toast.makeText(ComplaintDetailActivity.this, R.string.feedback_success, Toast.LENGTH_LONG).show();
                        checkFeedback(complaintId);
                    } else {
                        String msg = getString(R.string.feedback_submit_failed);
                        try {
                            JSONObject err = new JSONObject(responseBody);
                            if (err.has("message")) msg = err.getString("message");
                        } catch (Exception ignored) {}
                        Toast.makeText(ComplaintDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    btnSubmitFeedback.setEnabled(true);
                }
            });
        } catch (Exception ignored) {}
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
                            String changedBy = obj.optString("changedBy", getString(R.string.system_user));
                            String changedAt = obj.optString("changedAt", "");
                            String remarks = obj.optString("remarks", "");

                            if (changedAt.length() >= 16) changedAt = changedAt.substring(0, 16).replace("T", " ");

                            String remarksStr = remarks.isEmpty() ? "" : getString(R.string.remarks_prefix) + remarks;
                            TextView historyItem = new TextView(ComplaintDetailActivity.this);
                            historyItem.setText(getString(R.string.history_item_fmt, newStatus, changedBy, changedAt, remarksStr));
                            historyItem.setPadding(0, 8, 0, 8);
                            historyItem.setTextColor(0xFF475569);
                            containerHistory.addView(historyItem);
                        }
                } catch (Exception e) {
                    android.util.Log.e("ComplaintDetailActivity", "Error parsing history", e);
                }
                }
            }

            @Override
        public void onError(Exception e) {
            android.util.Log.e("ComplaintDetailActivity", "Failed to fetch complaint history", e);
        }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }
}
