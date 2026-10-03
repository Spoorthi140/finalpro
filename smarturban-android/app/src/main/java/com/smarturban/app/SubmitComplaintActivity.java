package com.smarturban.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SubmitComplaintActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private EditText etTitle, etDescription;
    private Spinner spCategory;
    private Button btnGetLocation, btnSelectImage, btnSubmit;
    private TextView tvLocationDisplay;
    private ImageView imgPreview;

    private double latitude = 12.9716;
    private double longitude = 77.5946;
    private String locationName = "MG Road, Bengaluru (12.9716, 77.5946)";

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

        String[] categories = new String[]{
                "Road Maintenance",
                "Streetlights",
                "Sanitation/Garbage",
                "Water Supply",
                "Drainage",
                "Other Urban Infrastructure"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spCategory.setAdapter(adapter);

        btnGetLocation.setOnClickListener(v -> {
            tvLocationDisplay.setText("Location: " + locationName);
            Toast.makeText(this, "GPS Location Captured Successfully", Toast.LENGTH_SHORT).show();
        });

        btnSelectImage.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, "Camera simulation enabled", Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDescription.getText().toString().trim();

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, R.string.field_required, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, R.string.complaint_submitted, Toast.LENGTH_LONG).show();
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.get("data") != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imgPreview.setImageBitmap(imageBitmap);
                imgPreview.setVisibility(ImageView.VISIBLE);
            }
        }
    }
}
