package com.smarturban.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyComplaintsActivity extends AppCompatActivity {

    private ListView lvComplaints;

    static class ComplaintItem {
        long id;
        String title;
        String category;
        String status;
        String date;

        ComplaintItem(long id, String title, String category, String status, String date) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.status = status;
            this.date = date;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppLocaleManager.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_complaints);

        lvComplaints = findViewById(R.id.lv_complaints);

        loadComplaintsFromBackend();
    }

    private void loadComplaintsFromBackend() {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        HttpNetworkClient.sendJsonRequest(ApiConfig.MY_COMPLAINTS_URL, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONArray array = new JSONArray(responseBody);
                        List<ComplaintItem> list = new ArrayList<>();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            long id = obj.getLong("id");
                            String title = obj.getString("title");
                            String status = obj.optString("status", "Submitted");
                            String date = obj.optString("createdAt", "");
                            if (date.length() >= 10) date = date.substring(0, 10);

                            String category = "General";
                            if (obj.has("category") && !obj.isNull("category")) {
                                category = obj.getJSONObject("category").optString("name", "General");
                            }

                            list.add(new ComplaintItem(id, title, category, status, date));
                        }

                        displayComplaints(list);
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    private void displayComplaints(List<ComplaintItem> list) {
        ArrayAdapter<ComplaintItem> adapter = new ArrayAdapter<ComplaintItem>(this, 0, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_complaint, parent, false);
                }
                ComplaintItem item = getItem(position);

                TextView tvId = convertView.findViewById(R.id.tv_cmp_id);
                TextView tvTitle = convertView.findViewById(R.id.tv_cmp_title);
                TextView tvCat = convertView.findViewById(R.id.tv_cmp_category);
                TextView tvStatus = convertView.findViewById(R.id.tv_cmp_status);
                TextView tvDate = convertView.findViewById(R.id.tv_cmp_date);

                if (item != null) {
                    tvId.setText("#CMP-" + item.id);
                    tvTitle.setText(item.title);
                    tvCat.setText(getString(R.string.category) + ": " + item.category);
                    tvStatus.setText(item.status);
                    tvDate.setText("Date: " + item.date);
                }

                return convertView;
            }
        };

        lvComplaints.setAdapter(adapter);

        lvComplaints.setOnItemClickListener((parent, view, position, id) -> {
            ComplaintItem selected = list.get(position);
            Intent intent = new Intent(MyComplaintsActivity.this, ComplaintDetailActivity.class);
            intent.putExtra("complaint_id", selected.id);
            startActivity(intent);
        });
    }
}
