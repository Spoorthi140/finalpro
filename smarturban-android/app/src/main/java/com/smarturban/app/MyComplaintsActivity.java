package com.smarturban.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MyComplaintsActivity extends AppCompatActivity {

    private ListView lvComplaints;

    static class DummyComplaint {
        long id;
        String title;
        String category;
        String status;
        String date;

        DummyComplaint(long id, String title, String category, String status, String date) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.status = status;
            this.date = date;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_complaints);

        lvComplaints = findViewById(R.id.lv_complaints);

        List<DummyComplaint> list = new ArrayList<>();
        list.add(new DummyComplaint(1, "Large Pothole on Commercial Street", "Road Maintenance", "Submitted", "2026-10-03"));
        list.add(new DummyComplaint(2, "Streetlight Not Working in Ward 12", "Streetlights", "In Progress", "2026-10-02"));
        list.add(new DummyComplaint(3, "Garbage Overflow near Park Gate", "Sanitation/Garbage", "Resolved", "2026-09-28"));

        ArrayAdapter<DummyComplaint> adapter = new ArrayAdapter<DummyComplaint>(this, 0, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_complaint, parent, false);
                }
                DummyComplaint item = getItem(position);

                TextView tvId = convertView.findViewById(R.id.tv_cmp_id);
                TextView tvTitle = convertView.findViewById(R.id.tv_cmp_title);
                TextView tvCat = convertView.findViewById(R.id.tv_cmp_category);
                TextView tvStatus = convertView.findViewById(R.id.tv_cmp_status);
                TextView tvDate = convertView.findViewById(R.id.tv_cmp_date);

                if (item != null) {
                    tvId.setText("#CMP-" + item.id);
                    tvTitle.setText(item.title);
                    tvCat.setText("Category: " + item.category);
                    tvStatus.setText(item.status);
                    tvDate.setText("Date: " + item.date);
                }

                return convertView;
            }
        };

        lvComplaints.setAdapter(adapter);

        lvComplaints.setOnItemClickListener((parent, view, position, id) -> {
            DummyComplaint selected = list.get(position);
            Intent intent = new Intent(MyComplaintsActivity.this, ComplaintDetailActivity.class);
            intent.putExtra("complaint_id", selected.id);
            intent.putExtra("title", selected.title);
            intent.putExtra("category", selected.category);
            intent.putExtra("status", selected.status);
            intent.putExtra("date", selected.date);
            startActivity(intent);
        });
    }
}
