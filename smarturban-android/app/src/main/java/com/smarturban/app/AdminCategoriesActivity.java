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

public class AdminCategoriesActivity extends AppCompatActivity {

    private EditText etName, etDesc;
    private Button btnAdd;
    private ListView lvCategories;

    static class CategoryItem {
        long id;
        String name;
        String desc;

        CategoryItem(long id, String name, String desc) {
            this.id = id;
            this.name = name;
            this.desc = desc;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_categories);

        etName = findViewById(R.id.et_cat_name);
        etDesc = findViewById(R.id.et_cat_desc);
        btnAdd = findViewById(R.id.btn_add_category);
        lvCategories = findViewById(R.id.lv_admin_categories);

        loadCategories();

        btnAdd.setOnClickListener(v -> addCategory());
    }

    private void loadCategories() {
        SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        HttpNetworkClient.sendJsonRequest(ApiConfig.ADMIN_CATEGORIES_URL, "GET", null, jwtToken, new HttpNetworkClient.ApiResponseCallback() {
            @Override
            public void onSuccess(int statusCode, String responseBody) {
                if (statusCode == 200 && responseBody != null) {
                    try {
                        JSONArray array = new JSONArray(responseBody);
                        List<CategoryItem> list = new ArrayList<>();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            list.add(new CategoryItem(obj.getLong("id"), obj.getString("name"), obj.optString("description", "")));
                        }

                        displayCategories(list);
                    } catch (Exception e) {
                        Toast.makeText(AdminCategoriesActivity.this, "Error parsing categories", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    private void displayCategories(List<CategoryItem> list) {
        ArrayAdapter<CategoryItem> adapter = new ArrayAdapter<CategoryItem>(this, android.R.layout.simple_list_item_2, android.R.id.text1, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                CategoryItem item = getItem(position);
                if (item != null) {
                    text1.setText(item.name);
                    text2.setText(item.desc);
                }
                return view;
            }
        };
        lvCategories.setAdapter(adapter);
    }

    private void addCategory() {
        String name = etName.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Category name required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("description", desc);

            SharedPreferences pref = getSharedPreferences("SmartUrbanPref", MODE_PRIVATE);
            String jwtToken = pref.getString("token", "");

            HttpNetworkClient.sendJsonRequest(ApiConfig.ADMIN_CATEGORIES_URL, "POST", json.toString(), jwtToken, new HttpNetworkClient.ApiResponseCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    if (statusCode == 200 || statusCode == 201) {
                        Toast.makeText(AdminCategoriesActivity.this, "Category added", Toast.LENGTH_SHORT).show();
                        etName.setText("");
                        etDesc.setText("");
                        loadCategories();
                    } else {
                        Toast.makeText(AdminCategoriesActivity.this, "Failed to add category", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {}
            });
        } catch (Exception ignored) {}
    }
}
