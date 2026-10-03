package com.smarturban.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdminComplaintDetailActivity extends AppCompatActivity {

    private TextView tvDetailId, tvDetailTitle, tvDetailCitizen, tvDetailDesc;
    private Spinner spStatus, spDepartment;
    private EditText etRemarks;
    private Button btnSave;

    private long complaintId = -1;
    private List<Long> departmentIds = new ArrayList<>();
    private List<String> departmentNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_complaint_detail);

        tvDetailId = findViewById(R.id.tv_admin_detail_id);
        tvDetailTitle = findViewById(R.id.tv_admin_detail_title);
        tvDetailCitizen = findViewById(R.id.tv_admin_detail_citizen);
        tvDetailDesc = findViewById(R.id.tv_admin_detail_desc);
        spStatus = findViewById(R.id.sp_admin_status);
        spDepartment = findViewById(R.id.sp_admin_department);
        etRemarks = findViewById(R.id.et_admin_remarks);
        btnSave = findViewById(R.id.btn_admin_save_complaint);

        complaintId = getIntent().getLongExtra("complaint_id", -1);

        setupStatusSpinner();
        loadDepartments();
        if (complaintId != -1) {
            loadComplaintDetails(complaintId);
        }

        btnSave.setOnClickListener(v -> saveComplaintUpdate());
    }

    private void setupStatusSpinner() {
        String[] statuses = new String[]{"Submitted", "Under Review", "Assigned", "In Progress", "Resolved", "Rejected"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spStatus.setAdapter(adapter);
    }

    private void loadDepartments() {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        HttpNetworkClient.sendJsonRequest(ApiConfig.DEPARTMENTS_URL, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONArray array = new JSONArray(responseBody);
                        departmentIds.clear();
                        departmentNames.clear();

                        departmentIds.add(0L);
                        departmentNames.add("-- Select Department --");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            departmentIds.add(obj.getLong("id"));
                            departmentNames.add(obj.getString("name"));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminComplaintDetailActivity.this, android.R.layout.simple_spinner_dropdown_item, departmentNames);
                        spDepartment.setAdapter(adapter);
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onError(Exception e) {}
        });
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
                        tvDetailDesc.setText(obj.getString("description"));

                        if (obj.has("user") && !obj.isNull("user")) {
                            JSONObject userObj = obj.getJSONObject("user");
                            tvDetailCitizen.setText("Citizen: " + userObj.optString("fullName", "N/A") + " (" + userObj.optString("email", "") + ")");
                        }

                        String currentStatus = obj.optString("status", "Submitted");
                        String[] statuses = new String[]{"Submitted", "Under Review", "Assigned", "In Progress", "Resolved", "Rejected"};
                        for (int i = 0; i < statuses.length; i++) {
                            if (statuses[i].equalsIgnoreCase(currentStatus)) {
                                spStatus.setSelection(i);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(AdminComplaintDetailActivity.this, "Error parsing details", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    private void saveComplaintUpdate() {
        if (complaintId == -1) return;

        String selectedStatus = spStatus.getSelectedItem().toString();
        int deptPos = spDepartment.getSelectedItemPosition();
        Long deptId = (deptPos > 0 && deptPos < departmentIds.size()) ? departmentIds.get(deptPos) : null;
        String remarks = etRemarks.getText().toString().trim();

        try {
            JSONObject json = new JSONObject();
            json.put("status", selectedStatus);
            if (deptId != null) json.put("departmentId", deptId);
            json.put("remarks", remarks);

            SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
            String jwtToken = pref.getString("token", "");

            String url = ApiConfig.ADMIN_COMPLAINTS_URL + "/" + complaintId + "/status";

            btnSave.setEnabled(false);

            HttpNetworkClient.sendJsonRequest(url, "PUT", json.toString(), jwtToken, new HttpNetworkClient.ApiResponseCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    btnSave.setEnabled(true);
                    if (statusCode == 200) {
                        Toast.makeText(AdminComplaintDetailActivity.this, "Complaint updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AdminComplaintDetailActivity.this, "Failed to update complaint (" + statusCode + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    btnSave.setEnabled(true);
                    Toast.makeText(AdminComplaintDetailActivity.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error preparing update", Toast.LENGTH_SHORT).show();
        }
    }
}
