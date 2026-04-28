package com.flatmate.app.notification;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * Saves (or updates) the FCM device token for the given user.
     * Called by the mobile app whenever it gets a new FCM token.
     */
    public void saveToken(String userId, String token) {
        DeviceToken deviceToken = new DeviceToken();
        deviceToken.setUserId(userId);
        deviceToken.setFcmToken(token);
        deviceTokenRepository.save(deviceToken);
    }

    /**
     * Sends a Firebase push notification to all registered devices for the user.
     * Falls back to console logging when Firebase is not initialized (local dev).
     */
    public void sendNotification(String userId, String title, String body) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(userId);

        if (tokens.isEmpty()) {
            log.warn("No device tokens found for user {}. Skipping push notification.", userId);
            return;
        }

        // Check if Firebase has been initialized (will be null in local dev without service account)
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("[FCM MOCK] No Firebase app initialized. Would send to user={} title='{}' body='{}'",
                    userId, title, body);
            return;
        }

        List<String> fcmTokens = tokens.stream()
                .map(DeviceToken::getFcmToken)
                .collect(Collectors.toList());

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(notification)
                .addAllTokens(fcmTokens)
                .build();

        try {
            var response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("FCM multicast to user={}: successCount={}, failureCount={}",
                    userId, response.getSuccessCount(), response.getFailureCount());

            // Log individual failures so stale tokens can be identified
            if (response.getFailureCount() > 0) {
                for (int i = 0; i < response.getResponses().size(); i++) {
                    var r = response.getResponses().get(i);
                    if (!r.isSuccessful()) {
                        log.warn("FCM failed for token {}: {}", fcmTokens.get(i),
                                r.getException() != null ? r.getException().getMessage() : "unknown");
                    }
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("FCM send failed for user={}: {}", userId, e.getMessage());
        }
    }
}
