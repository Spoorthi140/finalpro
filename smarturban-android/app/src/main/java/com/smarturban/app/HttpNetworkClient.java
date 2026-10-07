package com.smarturban.app;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpNetworkClient {

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ApiResponseCallback {
        void onSuccess(int statusCode, String responseBody);
        void onError(Exception e);
    }

    public static void sendJsonRequest(String urlString, String method, String jsonBody, String jwtToken, ApiResponseCallback callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                if (jwtToken != null && !jwtToken.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
                }

                if (jsonBody != null && ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method))) {
                    conn.setDoOutput(true);
                    try (OutputStream os = conn.getOutputStream();
                         OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8")) {
                        writer.write(jsonBody);
                        writer.flush();
                    }
                }

                int statusCode = conn.getResponseCode();
                InputStream is = (statusCode >= 200 && statusCode < 300) ? conn.getInputStream() : conn.getErrorStream();

                StringBuilder responseBuilder = new StringBuilder();
                if (is != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line);
                        }
                    }
                }

                String responseStr = responseBuilder.toString();
                mainHandler.post(() -> callback.onSuccess(statusCode, responseStr));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    public static void sendMultipartRequest(String urlString, String jwtToken, String title, String description, long categoryId,
                                           Double latitude, Double longitude, String locationName,
                                           byte[] imageBytes, String imageName, ApiResponseCallback callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            String boundary = "---SmartUrbanBoundary" + System.currentTimeMillis();
            String LINE_FEED = "\r\n";

            try {
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conn.setRequestProperty("Accept", "application/json");

                if (jwtToken != null && !jwtToken.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
                }

                OutputStream outputStream = conn.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

                // Add form fields
                addFormField(writer, outputStream, boundary, LINE_FEED, "title", title);
                addFormField(writer, outputStream, boundary, LINE_FEED, "description", description);
                addFormField(writer, outputStream, boundary, LINE_FEED, "categoryId", String.valueOf(categoryId));
                if (latitude != null) addFormField(writer, outputStream, boundary, LINE_FEED, "latitude", String.valueOf(latitude));
                if (longitude != null) addFormField(writer, outputStream, boundary, LINE_FEED, "longitude", String.valueOf(longitude));
                if (locationName != null) addFormField(writer, outputStream, boundary, LINE_FEED, "locationName", locationName);

                // Add image file if attached
                if (imageBytes != null && imageBytes.length > 0) {
                    writer.append("--").append(boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"").append(imageName != null ? imageName : "photo.jpg").append("\"").append(LINE_FEED);
                    writer.append("Content-Type: image/jpeg").append(LINE_FEED);
                    writer.append(LINE_FEED);
                    writer.flush();

                    outputStream.write(imageBytes);
                    outputStream.flush();

                    writer.append(LINE_FEED);
                    writer.flush();
                }

                writer.append("--").append(boundary).append("--").append(LINE_FEED);
                writer.close();

                int statusCode = conn.getResponseCode();
                InputStream is = (statusCode >= 200 && statusCode < 300) ? conn.getInputStream() : conn.getErrorStream();

                StringBuilder responseBuilder = new StringBuilder();
                if (is != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line);
                        }
                    }
                }

                String responseStr = responseBuilder.toString();
                mainHandler.post(() -> callback.onSuccess(statusCode, responseStr));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    private static void addFormField(PrintWriter writer, OutputStream outputStream, String boundary, String LINE_FEED, String name, String value) {
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }
}
