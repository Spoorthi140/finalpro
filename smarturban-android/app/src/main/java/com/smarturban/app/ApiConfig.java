package com.smarturban.app;

public class ApiConfig {
    // Centralized backend API base URL
    public static final String BASE_URL = "http://10.0.2.2:8080/api/";

    // Auth endpoints
    public static final String LOGIN_URL = BASE_URL + "auth/login";
    public static final String REGISTER_URL = BASE_URL + "auth/register";

    // Common Data endpoints
    public static final String CATEGORIES_URL = BASE_URL + "categories";
    public static final String DEPARTMENTS_URL = BASE_URL + "departments";

    // Citizen endpoints
    public static final String PROFILE_URL = BASE_URL + "users/me";
    public static final String SUBMIT_COMPLAINT_URL = BASE_URL + "citizen/complaints";
    public static final String MY_COMPLAINTS_URL = BASE_URL + "citizen/complaints";

    // Admin endpoints
    public static final String ADMIN_STATS_URL = BASE_URL + "admin/dashboard/stats";
    public static final String ADMIN_USERS_URL = BASE_URL + "admin/users";
    public static final String ADMIN_COMPLAINTS_URL = BASE_URL + "admin/complaints";
    public static final String ADMIN_CATEGORIES_URL = BASE_URL + "admin/categories";
    public static final String ADMIN_DEPARTMENTS_URL = BASE_URL + "admin/departments";
}
