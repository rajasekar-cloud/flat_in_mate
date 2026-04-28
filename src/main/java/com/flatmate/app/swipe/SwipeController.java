package com.flatmate.app.swipe;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/swipes")
@RequiredArgsConstructor
public class SwipeController {

    private final SwipeService swipeService;

    @PostMapping
    public ResponseEntity<?> recordSwipe(@RequestBody Swipe swipe) {
        try {
            swipeService.recordSwipe(swipe.getSeekerId(), swipe.getListingId(), swipe.getType());
            return ResponseEntity.ok("Swipe recorded");
        } catch (RuntimeException e) {
            // 409 Conflict for duplicate swipe so clients can handle it distinctly
            if (e.getMessage() != null && e.getMessage().contains("already swiped")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Returns the set of listing IDs this seeker has already swiped on (LEFT or RIGHT).
     * The mobile app calls this on feed-load and excludes these IDs so swiped profiles
     * are never shown again.
     *
     * GET /swipes/my-swipes?seekerId=+911234567890
     */
    @GetMapping("/my-swipes")
    public ResponseEntity<Set<String>> getMySwipes(@RequestParam String seekerId) {
        return ResponseEntity.ok(swipeService.getSwipedListingIds(seekerId));
    }

    @GetMapping("/listing/{listingId}/interests")
    public ResponseEntity<List<InterestedSeekerDTO>> getInterests(@PathVariable String listingId) {
        return ResponseEntity.ok(swipeService.getInterestedSeekers(listingId));
    }
}
