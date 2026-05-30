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
        
        // Default to REQUEST_SENT for formal join requests
        match.setStatus("REQUEST_SENT"); 
        
        match.setCreatedAt(LocalDateTime.now().toString());
        match.setUpdatedAt(LocalDateTime.now().toString());
        return matchRepository.save(match);
    }

    public Match markAsInterested(String seekerId, String ownerId) {
        Match match = new Match();
        match.setMatchId(UUID.randomUUID().toString());
        match.setSeekerId(seekerId);
        match.setOwnerId(ownerId);
        match.setStatus("INTERESTED");
        match.setCreatedAt(LocalDateTime.now().toString());
        match.setUpdatedAt(LocalDateTime.now().toString());
        return matchRepository.save(match);
    }

    public Match approveMatch(String seekerId, String ownerId) {
        Match match = matchRepository.findBySeekerIdAndOwnerId(seekerId, ownerId)
                .orElseThrow(() -> new RuntimeException("Match record not found"));

        match.setStatus("MATCHED"); // Figma: It's a match!
        match.setUpdatedAt(LocalDateTime.now().toString());
        Match saved = matchRepository.save(match);

        eventPublisher.publishEvent("match-events", "MatchApproved: seeker=" + seekerId + ", owner=" + ownerId);

        return saved;
    }

    public List<MatchResponse> getMyMatches(String userId) {
        return matchRepository.findBySeekerId(userId).stream()
                .map(m -> convertToResponse(m, userId))
                .toList();
    }

    private MatchResponse convertToResponse(Match match, String currentUserId) {
        // Determine the ID of the person the current user matched with
        String otherUserId = match.getSeekerId().equals(currentUserId) ? match.getOwnerId() : match.getSeekerId();
        var otherUser = userRepository.findById(otherUserId).orElse(null);

        MatchResponse resp = new MatchResponse();
        resp.setMatchId(match.getMatchId());
        resp.setOtherUserId(otherUserId);
        resp.setListingId(match.getListingId());
        resp.setStatus(match.getStatus());
        resp.setCreatedAt(match.getCreatedAt());

        if (otherUser != null) {
            String fullName = (otherUser.getFirstName() != null ? otherUser.getFirstName() : "") + " " + 
                              (otherUser.getLastName() != null ? otherUser.getLastName() : "");
            resp.setOtherUserName(fullName.trim().isEmpty() ? "Flatmate User" : fullName.trim());
            resp.setOtherUserProfilePic(otherUser.getProfilePic());
        } else {
            resp.setOtherUserName("Unknown User");
        }

        return resp;
    }

    public List<MatchSearchResult> searchMatches(String currentUserId, String query) {
        return matchRepository.findByUserId(currentUserId).stream()
                .map(match -> {
                    String otherUserId = match.getSeekerId().equals(currentUserId) 
                            ? match.getOwnerId() 
                            : match.getSeekerId();
                    var otherUser = userRepository.findById(otherUserId).orElse(null);
                    
                    Listing listing = null;
                    if (match.getListingId() != null) {
                        try {
                            listing = listingService.getListing(match.getListingId());
                        } catch (Exception e) {
                            // Listing not found or deleted
                        }
                    }

                    String otherUserName = "Unknown User";
                    String otherUserProfilePic = null;
                    if (otherUser != null) {
                        String fullName = (otherUser.getFirstName() != null ? otherUser.getFirstName() : "") + " " + 
                                          (otherUser.getLastName() != null ? otherUser.getLastName() : "");
                        otherUserName = fullName.trim().isEmpty() ? "Flatmate User" : fullName.trim();
                        otherUserProfilePic = otherUser.getProfilePic();
                    }

                    // Apply the search query filter
                    if (query != null && !query.isBlank()) {
                        String q = query.toLowerCase();
                        boolean matchesName = otherUserName.toLowerCase().contains(q);
                        boolean matchesListing = false;
                        if (listing != null) {
                            matchesListing = listingService.matchesSearch(listing, query);
                        }
                        if (!matchesName && !matchesListing) {
                            return null; // Exclude from search results
                        }
                    }

                    MatchSearchResult result = new MatchSearchResult();
                    result.setMatch(match);
                    result.setOtherUserId(otherUserId);
                    result.setOtherUserName(otherUserName);
                    result.setOtherUserProfilePic(otherUserProfilePic);
                    result.setListing(listing);
                    return result;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}

