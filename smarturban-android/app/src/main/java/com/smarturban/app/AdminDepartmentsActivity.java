package com.smarturban.app;

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

public class AdminDepartmentsActivity extends AppCompatActivity {

    private EditText etName, etDesc;
    private Button btnAdd;
    private ListView lvDepartments;

    static class DepartmentItem {
        long id;
        String name;
        String desc;

        DepartmentItem(long id, String name, String desc) {
            this.id = id;
            this.name = name;
            this.desc = desc;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_departments);

        etName = findViewById(R.id.et_dept_name);
        etDesc = findViewById(R.id.et_dept_desc);
        btnAdd = findViewById(R.id.btn_add_department);
        lvDepartments = findViewById(R.id.lv_admin_departments);

        loadDepartments();

        btnAdd.setOnClickListener(v -> addDepartment());
    }

    private void loadDepartments() {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        HttpNetworkClient.sendJsonRequest(ApiConfig.ADMIN_DEPARTMENTS_URL, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONArray array = new JSONArray(responseBody);
                        List<DepartmentItem> list = new ArrayList<>();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new DepartmentItem(obj.getLong("id"), obj.getString("name"), obj.optString("description", "")));
                        }

                        displayDepartments(list);
                    } catch (Exception e) {
                        Toast.makeText(AdminDepartmentsActivity.this, "Error parsing departments", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    private void displayDepartments(List<DepartmentItem> list) {
        ArrayAdapter<DepartmentItem> adapter = new ArrayAdapter<DepartmentItem>(this, android.R.layout.simple_list_item_2, android.R.id.text1, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                DepartmentItem item = getItem(position);
                if (item != null) {
                    text1.setText(item.name);
                    text2.setText(item.desc);
                }
                return view;
            }
        };
        lvDepartments.setAdapter(adapter);
    }

    private void addDepartment() {
        String name = etName.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Department name required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("description", desc);

            SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
            String jwtToken = pref.getString("token", "");

            HttpNetworkClient.sendJsonRequest(ApiConfig.ADMIN_DEPARTMENTS_URL, "POST", json.toString(), jwtToken, new HttpNetworkClient.ApiResponseCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    if (statusCode == 200 || statusCode == 201) {
                        Toast.makeText(AdminDepartmentsActivity.this, "Department added", Toast.LENGTH_SHORT).show();
                        etName.setText("");
                        etDesc.setText("");
                        loadDepartments();
                    } else {
                        Toast.makeText(AdminDepartmentsActivity.this, "Failed to add department", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {}
            });
        } catch (Exception ignored) {}
    }
}
