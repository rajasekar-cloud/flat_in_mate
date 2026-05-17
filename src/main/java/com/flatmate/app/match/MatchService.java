package com.flatmate.app.match;

import com.flatmate.app.auth.User;
import com.flatmate.app.auth.UserRepository;
import com.flatmate.app.listing.Listing;
import com.flatmate.app.listing.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ListingService listingService;
    private final com.flatmate.app.event.EventPublisher eventPublisher;

    public Match createMatchRequest(String seekerId, String ownerId, String listingId) {
        Match match = new Match();
        match.setMatchId(UUID.randomUUID().toString());
        match.setSeekerId(seekerId);
        match.setOwnerId(ownerId);
        match.setListingId(listingId);
        match.setStatus("PENDING");
        match.setCreatedAt(LocalDateTime.now().toString());
        match.setUpdatedAt(LocalDateTime.now().toString());
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
        return matchRepository.findByUserId(userId);
    }

    public List<MatchSearchResult> searchMatches(String userId, String query) {
        return matchRepository.findByUserId(userId).stream()
                .map(match -> toSearchResult(userId, match))
                .filter(result -> matchesResult(result, query))
                .toList();
    }

    private MatchSearchResult toSearchResult(String currentUserId, Match match) {
        String otherUserId = currentUserId.equals(match.getSeekerId())
                ? match.getOwnerId()
                : match.getSeekerId();

        User otherUser = userRepository.findById(otherUserId).orElse(null);
        Listing listing = null;
        if (match.getListingId() != null) {
            try {
                listing = listingService.getListing(match.getListingId());
            } catch (RuntimeException ignored) {
                listing = null;
            }
        }

        MatchSearchResult result = new MatchSearchResult();
        result.setMatch(match);
        result.setOtherUserId(otherUserId);
        result.setOtherUserName(displayName(otherUser, otherUserId));
        result.setOtherUserProfilePic(otherUser != null ? otherUser.getProfilePic() : null);
        result.setListing(listing);
        return result;
    }

    private boolean matchesResult(MatchSearchResult result, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String q = query.toLowerCase();
        Listing listing = result.getListing();
        return contains(result.getOtherUserName(), q)
                || contains(result.getOtherUserId(), q)
                || contains(result.getMatch().getStatus(), q)
                || (listing != null && listingService.matchesSearch(listing, query));
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
