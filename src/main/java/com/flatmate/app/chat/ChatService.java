package com.flatmate.app.chat;

import com.flatmate.app.auth.User;
import com.flatmate.app.auth.UserRepository;
import com.flatmate.app.match.Match;
import com.flatmate.app.match.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

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

    public List<ChatContactSearchResult> searchChatContacts(String currentUserId, String query) {
        return matchRepository.findByUserId(currentUserId).stream()
                .map(match -> toChatContact(currentUserId, match))
                .filter(Objects::nonNull)
                .filter(result -> matchesContact(result, query))
                .toList();
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

    private ChatContactSearchResult toChatContact(String currentUserId, Match match) {
        List<Message> history = getChatHistory(match.getMatchId());
        if (history.isEmpty()) {
            return null;
        }

        String otherUserId = currentUserId.equals(match.getSeekerId())
                ? match.getOwnerId()
                : match.getSeekerId();
        User otherUser = userRepository.findById(otherUserId).orElse(null);
        Message lastMessage = history.get(history.size() - 1);

        ChatContactSearchResult result = new ChatContactSearchResult();
        result.setMatchId(match.getMatchId());
        result.setUserId(otherUserId);
        result.setName(displayName(otherUser, otherUserId));
        result.setFirstName(otherUser != null ? otherUser.getFirstName() : null);
        result.setLastName(otherUser != null ? otherUser.getLastName() : null);
        result.setProfilePic(otherUser != null ? otherUser.getProfilePic() : null);
        result.setLastMessage(lastMessage.getContent());
        result.setLastMessageAt(lastMessage.getCreatedAt());
        return result;
    }

    private boolean matchesContact(ChatContactSearchResult result, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String q = query.toLowerCase();
        return contains(result.getName(), q)
                || contains(result.getFirstName(), q)
                || contains(result.getLastName(), q)
                || contains(result.getUserId(), q);
    }

    private String displayName(User user, String fallback) {
        if (user == null) {
            return fallback;
        }

        String first = user.getFirstName();
        String last = user.getLastName();
        if (first != null && last != null) return (first + " " + last).trim();
        if (first != null) return first;
        if (last != null) return last;
        if (user.getName() != null) return user.getName();
        return fallback;
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }
}
