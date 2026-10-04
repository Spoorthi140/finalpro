package com.smarturban.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

public class MyFirebaseMessagingService {

    public static void registerDeviceToken(Context context, String fcmToken) {
        SharedPreferences pref = context.getSharedPreferences("SmartUrbanPref", Context.MODE_PRIVATE);
        String jwtToken = pref.getString("token", "");

        if (jwtToken.isEmpty() || fcmToken == null || fcmToken.isEmpty()) {
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("fcmToken", fcmToken);

            HttpNetworkClient.sendJsonRequest(ApiConfig.FCM_TOKEN_URL, "POST", json.toString(), jwtToken, new HttpNetworkClient.ApiResponseCallback() {
                @Override public void onSuccess(int statusCode, String responseBody) {}
                @Override public void onError(Exception e) {}
            });
        } catch (Exception ignored) {}
    }

    public static void displayNotification(Context context, String title, String body, long complaintId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "smarturban_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "SmartUrban Status Updates", NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(context, ComplaintDetailActivity.class);
        intent.putExtra("complaint_id", complaintId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) complaintId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.smarturban_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
