package com.flatmate.app.match;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final com.flatmate.app.event.EventPublisher eventPublisher;

    public Match createMatchRequest(String seekerId, String ownerId, String listingId) {
        Match match = Match.builder()
                .matchId(UUID.randomUUID().toString())
                .seekerId(seekerId)
                .ownerId(ownerId)
                .listingId(listingId)
                .status("PENDING")
                .createdAt(LocalDateTime.now().toString())
                .build();
        return matchRepository.save(match);
    }

    public Match approveMatch(String seekerId, String ownerId) {
        Match match = matchRepository.findBySeekerIdAndOwnerId(seekerId, ownerId)
                .orElseThrow(() -> new RuntimeException("Match request not found"));

        match.setStatus("APPROVED");
        match.setUpdatedAt(LocalDateTime.now().toString());
        Match saved = matchRepository.save(match);

        eventPublisher.publishEvent("match-events", "MatchApproved: seeker=" + seekerId + ", owner=" + ownerId);

        return saved;
    }

    public List<Match> getMyMatches(String userId) {
        return matchRepository.findBySeekerId(userId);
    }
}
