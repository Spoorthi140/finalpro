package com.smarturban.app;

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

public class AdminComplaintsActivity extends AppCompatActivity {

    private ListView lvAdminComplaints;

    static class AdminComplaintItem {
        long id;
        String title;
        String category;
        String citizen;
        String status;
        String date;

        AdminComplaintItem(long id, String title, String category, String citizen, String status, String date) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.citizen = citizen;
            this.status = status;
            this.date = date;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_complaints);

        lvAdminComplaints = findViewById(R.id.lv_admin_complaints);
        loadAdminComplaints();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAdminComplaints();
    }

    private void loadAdminComplaints() {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        HttpNetworkClient.sendJsonRequest(ApiConfig.ADMIN_COMPLAINTS_URL, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONArray array = new JSONArray(responseBody);
                        List<AdminComplaintItem> list = new ArrayList<>();

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

                            String citizenName = "Citizen";
                            if (obj.has("user") && !obj.isNull("user")) {
                                citizenName = obj.getJSONObject("user").optString("fullName", "Citizen");
                            }

                            list.add(new AdminComplaintItem(id, title, category, citizenName, status, date));
                        }

                        displayComplaints(list);
                    } catch (Exception e) {
                        Toast.makeText(AdminComplaintsActivity.this, "Error parsing admin complaints", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminComplaintsActivity.this, "Failed to load complaints (" + statusCode + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminComplaintsActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayComplaints(List<AdminComplaintItem> list) {
        ArrayAdapter<AdminComplaintItem> adapter = new ArrayAdapter<AdminComplaintItem>(this, 0, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_complaint, parent, false);
                }
                AdminComplaintItem item = getItem(position);

                TextView tvId = convertView.findViewById(R.id.tv_cmp_id);
                TextView tvTitle = convertView.findViewById(R.id.tv_cmp_title);
                TextView tvCat = convertView.findViewById(R.id.tv_cmp_category);
                TextView tvStatus = convertView.findViewById(R.id.tv_cmp_status);
                TextView tvDate = convertView.findViewById(R.id.tv_cmp_date);

                if (item != null) {
                    tvId.setText("#CMP-" + item.id);
                    tvTitle.setText(item.title);
                    tvCat.setText("Category: " + item.category + " | Citizen: " + item.citizen);
                    tvStatus.setText(item.status);
                    tvDate.setText("Date: " + item.date);
                }

                return convertView;
            }
        };

        lvAdminComplaints.setAdapter(adapter);

        lvAdminComplaints.setOnItemClickListener((parent, view, position, id) -> {
            AdminComplaintItem selected = list.get(position);
            Intent intent = new Intent(AdminComplaintsActivity.this, AdminComplaintDetailActivity.class);
            intent.putExtra("complaint_id", selected.id);
            startActivity(intent);
        });
    }
}
