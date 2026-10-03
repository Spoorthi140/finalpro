package com.smarturban.app;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

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

        long cmpId = getIntent().getLongExtra("complaint_id", 1);
        String title = getIntent().getStringExtra("title");
        String status = getIntent().getStringExtra("status");

        tvDetailId.setText("#CMP-" + cmpId);
        tvDetailTitle.setText(title != null ? title : "Infrastructure Issue");
        tvDetailStatus.setText(status != null ? status : "Submitted");
        tvDetailDesc.setText("The infrastructure complaint was registered with description notes and location coordinates attached.");
        tvDetailLocation.setText("MG Road, Bengaluru (Lat: 12.9716, Long: 77.5946)");

        // Add history item
        TextView historyItem = new TextView(this);
        historyItem.setText("• Status set to [" + (status != null ? status : "Submitted") + "] on 2026-10-03");
        historyItem.setPadding(0, 8, 0, 8);
        historyItem.setTextColor(0xFF475569);
        containerHistory.addView(historyItem);
    }
}
