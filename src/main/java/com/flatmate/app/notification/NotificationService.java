package com.flatmate.app.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final DeviceTokenRepository deviceTokenRepository;

    public void saveToken(String userId, String token) {
        DeviceToken deviceToken = DeviceToken.builder()
                .userId(userId)
                .fcmToken(token)
                .build();
        deviceTokenRepository.save(deviceToken);
    }

    public void sendNotification(String userId, String title, String body) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(userId);
        for (DeviceToken token : tokens) {
            System.out.println(
                    "Sending notification to user " + userId + " [" + token.getFcmToken() + "]: " + title + " - "
                            + body);
        }
    }
}
