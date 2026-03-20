package com.flatmate.app.chat;

import com.flatmate.app.match.Match;
import com.flatmate.app.match.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MatchRepository matchRepository;

    public Message sendMessage(Message message) {
        message.setCreatedAt(LocalDateTime.now().toString());
        Message saved = messageRepository.save(message);

        // Broadcast via WebSocket
        messagingTemplate.convertAndSend("/topic/match/" + message.getMatchId(), saved);

        return saved;
    }

    public List<Message> getChatHistory(String matchId) {
        return messageRepository.findByMatchIdOrderByCreatedAtAsc(matchId);
    }

    public void markAsRead(String seekerId, String ownerId, String role) {
        Match match = matchRepository.findBySeekerIdAndOwnerId(seekerId, ownerId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        String now = LocalDateTime.now().toString();
        if ("SEEKER".equalsIgnoreCase(role)) {
            match.setSeekerLastRead(now);
        } else {
            match.setOwnerLastRead(now);
        }
        matchRepository.save(match);

        // Notify other party
        messagingTemplate.convertAndSend("/topic/match/" + match.getMatchId() + "/read", role);
    }

    public void sendTypingIndicator(String matchId, String senderId, boolean isTyping) {
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("matchId", matchId);
        payload.put("senderId", senderId);
        payload.put("isTyping", isTyping);

        messagingTemplate.convertAndSend("/topic/match/" + matchId + "/typing", payload);
    }
}
