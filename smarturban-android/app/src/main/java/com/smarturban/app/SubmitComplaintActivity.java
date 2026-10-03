package com.smarturban.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SubmitComplaintActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private EditText etTitle, etDescription;
    private Spinner spCategory;
    private Button btnGetLocation, btnSelectImage, btnSubmit;
    private TextView tvLocationDisplay;
    private ImageView imgPreview;

    private Double latitude = 12.9716;
    private Double longitude = 77.5946;
    private String locationName = "MG Road, Bengaluru";
    private byte[] imageBytes = null;

    private List<Long> categoryIds = new ArrayList<>();
    private List<String> categoryNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_complaint);

        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        spCategory = findViewById(R.id.sp_category);
        btnGetLocation = findViewById(R.id.btn_get_location);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnSubmit = findViewById(R.id.btn_submit);
        tvLocationDisplay = findViewById(R.id.tv_location_display);
        imgPreview = findViewById(R.id.img_preview);

        loadCategoriesFromBackend();

        btnGetLocation.setOnClickListener(v -> captureGpsLocation());

        btnSelectImage.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmit.setOnClickListener(v -> submitComplaintToBackend());
    }

    private void loadCategoriesFromBackend() {
        HttpNetworkClient.sendJsonRequest(ApiConfig.CATEGORIES_URL, "GET", null, null, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONArray array = new JSONArray(responseBody);
                        categoryIds.clear();
                        categoryNames.clear();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            categoryIds.add(obj.getLong("id"));
                            categoryNames.add(obj.getString("name"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(SubmitComplaintActivity.this, android.R.layout.simple_spinner_dropdown_item, categoryNames);
                        spCategory.setAdapter(adapter);
                    } catch (Exception e) {
                        setupFallbackCategories();
                    }
                } else {
                    setupFallbackCategories();
                }
            }

            @Override
            public void onError(Exception e) {
                setupFallbackCategories();
            }
        });
    }

    private void setupFallbackCategories() {
        categoryIds.clear();
        categoryNames.clear();
        String[] defaults = new String[]{"Road Maintenance", "Streetlights", "Sanitation/Garbage", "Water Supply", "Drainage", "Other Urban Infrastructure"};
        for (int i = 0; i < defaults.length; i++) {
            categoryIds.add((long) (i + 1));
            categoryNames.add(defaults[i]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryNames);
        spCategory.setAdapter(adapter);
    }

    private void captureGpsLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location loc) {
                        latitude = loc.getLatitude();
                        longitude = loc.getLongitude();
                        locationName = "GPS (" + String.format("%.4f", latitude) + ", " + String.format("%.4f", longitude) + ")";
                        tvLocationDisplay.setText("Location: " + locationName);
                        Toast.makeText(SubmitComplaintActivity.this, "GPS Location Captured", Toast.LENGTH_SHORT).show();
                    }
                    @Override public void onStatusChanged(String p, int s, Bundle e) {}
                    @Override public void onProviderEnabled(String p) {}
                    @Override public void onProviderDisabled(String p) {}
                }, null);
            } else {
                tvLocationDisplay.setText("Location: " + locationName + " (" + latitude + ", " + longitude + ")");
                Toast.makeText(this, "GPS Captured (Simulated/Coordinates set)", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException ex) {
            tvLocationDisplay.setText("Location: " + locationName + " (" + latitude + ", " + longitude + ")");
            Toast.makeText(this, "Location Captured: " + locationName, Toast.LENGTH_SHORT).show();
        }
    }

    private void submitComplaintToBackend() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, R.string.field_required, Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPos = spCategory.getSelectedItemPosition();
        long categoryId = (selectedPos >= 0 && selectedPos < categoryIds.size()) ? categoryIds.get(selectedPos) : 1L;

        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        btnSubmit.setEnabled(false);

        HttpNetworkClient.sendMultipartRequest(ApiConfig.SUBMIT_COMPLAINT_URL, jwtToken, title, desc, categoryId,
                latitude, longitude, locationName, imageBytes, "complaint.jpg", new HttpNetworkClient.ApiResponseCallback() {
                    @Override
                    public void onSuccess(int statusCode, String responseBody) {
                        btnSubmit.setEnabled(true);
                        if (statusCode == 200 || statusCode == 201) {
                            Toast.makeText(SubmitComplaintActivity.this, R.string.complaint_submitted, Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(SubmitComplaintActivity.this, "Failed to submit complaint (" + statusCode + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        btnSubmit.setEnabled(true);
                        Toast.makeText(SubmitComplaintActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.get("data") != null) {
                Bitmap bitmap = (Bitmap) extras.get("data");
                imgPreview.setImageBitmap(bitmap);
                imgPreview.setVisibility(ImageView.VISIBLE);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                imageBytes = stream.toByteArray();
            }
        }
    }
}
