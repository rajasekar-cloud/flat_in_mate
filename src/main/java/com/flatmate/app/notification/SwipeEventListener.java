package com.flatmate.app.notification;

import com.flatmate.app.listing.Listing;
import com.flatmate.app.listing.ListingService;
import com.flatmate.app.auth.User;
import com.flatmate.app.auth.UserRepository;
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
    private final ListingService listingService;
    private final UserRepository userRepository;

    @EventListener
    public void handleSwipeCreated(String event) {
        if (event.contains("SwipeCreated") && event.contains("seeker=")) {
            // Parse event: SwipeCreated: seeker=S123, listing=L456
            String seekerId = event.split("seeker=")[1].split(",")[0];
            String listingId = event.split("listing=")[1];

            // 1. Look up Listing & Seeker details
            Listing listing = listingService.getListing(listingId);
            User seeker = userRepository.findById(seekerId).orElse(null);

            if (listing == null) return;

            String ownerId = listing.getOwnerId();
            String propName = listing.getPropertyName() != null ? listing.getPropertyName() : "your listing";
            String seekerName = seeker != null ? seeker.getFirstName() : "Someone";

            String title = "New Interest in " + propName + "!";
            String body = seekerName + " swiped right on your property. Check their profile to match!";

            // 2. Send Push Notification (Mock)
            notificationService.sendNotification(ownerId, title, body);

            // 3. Persist Notification for In-App Inbox
            Notification notif = new Notification();
            notif.setId(UUID.randomUUID().toString());
            notif.setUserId(ownerId);
            notif.setTitle(title);
            notif.setMessage(body);
            notif.setTimestamp(LocalDateTime.now().toString());
            notif.setRead(false);
            notificationRepository.save(notif);
        }
    }
}
