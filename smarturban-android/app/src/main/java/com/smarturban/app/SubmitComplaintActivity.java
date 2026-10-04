package com.smarturban.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SubmitComplaintActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_IMAGE_GALLERY = 102;

    private ImageView btnBack;
    private EditText etTitle, etDescription;
    private Spinner spCategory;
    private Button btnCamera, btnGallery, btnRemovePhoto, btnUseCurrentLocation, btnConfirmLocation, btnSubmit;
    private TextView tvLocationDisplay;
    private RelativeLayout layoutPreviewContainer;
    private ImageView imgPreview;
    private MapView mapView;

    private Double latitude = null;
    private Double longitude = null;
    private String locationName = null;
    private boolean locationConfirmed = false;
    private byte[] imageBytes = null;

    private List<Long> categoryIds = new ArrayList<>();
    private List<String> categoryNames = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppLocaleManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));

        setContentView(R.layout.activity_submit_complaint);

        btnBack = findViewById(R.id.btn_back_report);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        spCategory = findViewById(R.id.sp_category);

        btnCamera = findViewById(R.id.btn_camera);
        btnGallery = findViewById(R.id.btn_gallery);
        btnRemovePhoto = findViewById(R.id.btn_remove_photo);
        layoutPreviewContainer = findViewById(R.id.layout_preview_container);
        imgPreview = findViewById(R.id.img_preview);

        btnUseCurrentLocation = findViewById(R.id.btn_use_current_location);
        tvLocationDisplay = findViewById(R.id.tv_location_display);
        mapView = findViewById(R.id.map_view_report);
        btnConfirmLocation = findViewById(R.id.btn_confirm_location);

        btnSubmit = findViewById(R.id.btn_submit);

        btnBack.setOnClickListener(v -> finish());

        loadCategoriesFromBackend();

        btnCamera.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });

        btnGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
        });

        btnRemovePhoto.setOnClickListener(v -> {
            imageBytes = null;
            layoutPreviewContainer.setVisibility(View.GONE);
        });

        btnUseCurrentLocation.setOnClickListener(v -> captureGpsLocation());

        btnConfirmLocation.setOnClickListener(v -> {
            if (latitude != null && longitude != null) {
                locationConfirmed = true;
                Toast.makeText(this, R.string.location_confirmed_msg, Toast.LENGTH_SHORT).show();
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
            if (locationManager == null) {
                Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_LONG).show();
                return;
            }

            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!gpsEnabled && !networkEnabled) {
                Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_LONG).show();
                tvLocationDisplay.setText(R.string.location_unavailable);
                return;
            }

            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location loc) {
                    if (loc != null) {
                        updateLocationData(loc.getLatitude(), loc.getLongitude());
                    }
                }
                @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override public void onProviderEnabled(String provider) {}
                @Override public void onProviderDisabled(String provider) {}
            };

            if (gpsEnabled) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            } else if (networkEnabled) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            }

            Location lastKnown = null;
            if (gpsEnabled) lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnown == null && networkEnabled) lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (lastKnown != null) {
                updateLocationData(lastKnown.getLatitude(), lastKnown.getLongitude());
            }

        } catch (SecurityException ex) {
            Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void updateLocationData(double lat, double lng) {
        latitude = lat;
        longitude = lng;
        locationName = getAddressFromCoordinates(lat, lng);

        tvLocationDisplay.setText("📍 " + locationName + "\nLat: " + String.format("%.4f", lat) + ", Long: " + String.format("%.4f", lng));

        setupOpenStreetMap(lat, lng);
        btnConfirmLocation.setVisibility(View.VISIBLE);
        locationConfirmed = true;
    }

    private void setupOpenStreetMap(double lat, double lng) {
        if (mapView != null) {
            mapView.setVisibility(View.VISIBLE);
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);

            GeoPoint point = new GeoPoint(lat, lng);
            mapView.getController().setZoom(16.0);
            mapView.getController().setCenter(point);

            mapView.getOverlays().clear();
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(locationName);
            mapView.getOverlays().add(marker);
            mapView.invalidate();
        }
    }

    private String getAddressFromCoordinates(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                if (address.getLocality() != null) sb.append(address.getLocality()).append(", ");
                if (address.getAdminArea() != null) sb.append(address.getAdminArea());
                return sb.length() > 0 ? sb.toString() : "GPS Location";
            }
        } catch (Exception ignored) {}
        return "GPS (" + String.format("%.4f", lat) + ", " + String.format("%.4f", lng) + ")";
    }

    private void submitComplaintToBackend() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, R.string.field_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (latitude == null || longitude == null) {
            Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_LONG).show();
            return;
        }

        if (!locationConfirmed) {
            Toast.makeText(this, R.string.location_not_confirmed_msg, Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPos = spCategory.getSelectedItemPosition();
        long categoryId = (selectedPos >= 0 && selectedPos < categoryIds.size()) ? categoryIds.get(selectedPos) : 1L;

        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        btnSubmit.setEnabled(false);
        btnSubmit.setText(R.string.submitting);

        HttpNetworkClient.sendMultipartRequest(ApiConfig.SUBMIT_COMPLAINT_URL, jwtToken, title, desc, categoryId,
                latitude, longitude, locationName, imageBytes, "complaint.jpg", new HttpNetworkClient.ApiResponseCallback() {
                    @Override
                    public void onSuccess(int statusCode, String responseBody) {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText(R.string.submit_complaint_btn);
                        if (statusCode == 200 || statusCode == 201) {
                            Toast.makeText(SubmitComplaintActivity.this, R.string.complaint_submitted, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(SubmitComplaintActivity.this, MyComplaintsActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SubmitComplaintActivity.this, "Failed to submit complaint (" + statusCode + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText(R.string.submit_complaint_btn);
                        Toast.makeText(SubmitComplaintActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data.getExtras() != null) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null) {
                    processImageBitmap(bitmap);
                }
            } else if (requestCode == REQUEST_IMAGE_GALLERY && data.getData() != null) {
                Uri selectedUri = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedUri);
                    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        byteBuffer.write(buffer, 0, len);
                    }
                    imageBytes = byteBuffer.toByteArray();
                    imgPreview.setImageURI(selectedUri);
                    layoutPreviewContainer.setVisibility(View.VISIBLE);
                } catch (Exception ignored) {}
            }
        }
    }

    private void processImageBitmap(Bitmap bitmap) {
        imgPreview.setImageBitmap(bitmap);
        layoutPreviewContainer.setVisibility(View.VISIBLE);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        imageBytes = stream.toByteArray();
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
