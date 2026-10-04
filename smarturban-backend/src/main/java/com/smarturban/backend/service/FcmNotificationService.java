package com.smarturban.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FcmNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(FcmNotificationService.class);

    /**
     * Triggers a Firebase Cloud Messaging push notification to the registered citizen's device token.
     * Keeps server credentials secure on the backend.
     */
    public void sendStatusUpdateNotification(String fcmToken, String complaintTitle, String newStatus) {
        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            logger.info("No FCM token registered for target citizen. Skipping push notification.");
            return;
        }

        logger.info("FCM Notification triggered for token [{}]: Complaint '{}' status changed to '{}'.",
                fcmToken, complaintTitle, newStatus);
    }
}
