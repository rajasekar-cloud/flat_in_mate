package com.flatmate.app.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SwipeEventListener {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleSwipeCreated(String event) {
        if (event.contains("SwipeCreated") && event.contains("seeker=")) {
            // Parse event: SwipeCreated: seeker=S123, listing=L456
            String seekerId = event.split("seeker=")[1].split(",")[0];
            String listingId = event.split("listing=")[1];

            // In a real app, look up the owner of the listing
            String ownerId = "TODO_LOOKUP_OWNER";

            String title = "New Interest!";
            String body = "A seeker (ID: " + seekerId + ") swiped right on your listing (ID: " + listingId + ")";

            // 1. Send Push Notification (Mock)
            notificationService.sendNotification(ownerId, title, body);

            // 2. Persist Notification for In-App Inbox
            Notification notif = Notification.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(ownerId)
                    .title(title)
                    .message(body)
                    .timestamp(LocalDateTime.now().toString())
                    .read(false)
                    .build();
            notificationRepository.save(notif);
        }
    }
}
