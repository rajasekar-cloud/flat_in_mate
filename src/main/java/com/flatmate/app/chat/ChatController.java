package com.flatmate.app.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final com.flatmate.app.listing.ListingService listingService;

    @MessageMapping("/chat")
    public void sendMessage(@Payload Message message) {
        chatService.sendMessage(message);
    }

    @MessageMapping("/chat/typing")
    public void sendTypingIndicator(@Payload java.util.Map<String, String> payload) {
        // payload should contain matchId and isTyping
        chatService.sendTypingIndicator(payload.get("matchId"), payload.get("senderId"),
                Boolean.parseBoolean(payload.get("isTyping")));
    }

    @PostMapping("/chat/read")
    @ResponseBody
    public ResponseEntity<String> markAsRead(@RequestBody java.util.Map<String, String> request) {
        String seekerId = request.get("seekerId");
        String ownerId = request.get("ownerId");
        String role = request.get("role");
        chatService.markAsRead(seekerId, ownerId, role);
        return ResponseEntity.ok("Marked as read");
    }

    @GetMapping("/chat/upload-url")
    @ResponseBody
    public ResponseEntity<String> getChatUploadUrl(@RequestParam String fileName) {
        // Reuse ListingService's pre-signed URL logic
        return ResponseEntity.ok(listingService.generateUploadUrl("chat/" + fileName));
    }

    @GetMapping("/chat/{matchId}/history")
    @ResponseBody
    public ResponseEntity<List<Message>> getHistory(@PathVariable String matchId) {
        return ResponseEntity.ok(chatService.getChatHistory(matchId));
    }
}
