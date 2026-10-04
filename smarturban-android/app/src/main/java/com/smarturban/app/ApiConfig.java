package com.smarturban.app;

public class ApiConfig {
    /**
     * Centralized PC/Backend Host IP configuration.
     * Replace YOUR_PC_IP with your PC's actual LAN IP address (e.g. "192.168.1.100") when testing on a physical Android phone,
     * or use "10.0.2.2" when testing on the Android Studio emulator.
     */
    public static final String YOUR_PC_IP = "YOUR_PC_IP";
    public static final String SERVER_PORT = "8080";

    // Centralized Base API URL
    public static final String BASE_URL = "http://" + YOUR_PC_IP + ":" + SERVER_PORT + "/api/";

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

    // Admin endpoints (accessed via REST API)
    public static final String ADMIN_STATS_URL = BASE_URL + "admin/dashboard/stats";
    public static final String ADMIN_USERS_URL = BASE_URL + "admin/users";
    public static final String ADMIN_COMPLAINTS_URL = BASE_URL + "admin/complaints";
    public static final String ADMIN_CATEGORIES_URL = BASE_URL + "admin/categories";
    public static final String ADMIN_DEPARTMENTS_URL = BASE_URL + "admin/departments";
}
