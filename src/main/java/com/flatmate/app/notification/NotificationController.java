package com.flatmate.app.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @PostMapping("/token")
    public ResponseEntity<String> saveToken(@RequestBody Map<String, Object> request) {
        String userId = request.get("userId").toString();
        String token = (String) request.get("token");
        notificationService.saveToken(userId, token);
        return ResponseEntity.ok("Token saved successfully");
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody Map<String, Object> request) {
        String userId = request.get("userId").toString();
        String title = (String) request.get("title");
        String body = (String) request.get("body");
        notificationService.sendNotification(userId, title, body);
        return ResponseEntity.ok("Notification sent successfully");
    }

    @GetMapping("/inbox/{userId}")
    public ResponseEntity<List<Notification>> getInbox(@PathVariable String userId) {
        return ResponseEntity.ok(notificationRepository.findByUserId(userId));
    }
}
