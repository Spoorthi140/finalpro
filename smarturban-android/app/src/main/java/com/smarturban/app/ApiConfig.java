package com.smarturban.app;

public class ApiConfig {
    // Centralized backend API base URL
    public static final String BASE_URL = "http://10.0.2.2:8080/api/";

    // Auth endpoints
    public static final String LOGIN_URL = BASE_URL + "auth/login";
    public static final String REGISTER_URL = BASE_URL + "auth/register";

    // Data endpoints
    public static final String CATEGORIES_URL = BASE_URL + "categories";
    public static final String DEPARTMENTS_URL = BASE_URL + "departments";

    // Citizen endpoints
    public static final String PROFILE_URL = BASE_URL + "citizen/profile";
    public static final String SUBMIT_COMPLAINT_URL = BASE_URL + "citizen/complaints";
    public static final String MY_COMPLAINTS_URL = BASE_URL + "citizen/complaints";
    public static final String AI_RECOMMEND_CATEGORY_URL = BASE_URL + "citizen/complaints/ai-recommend-category";
}
