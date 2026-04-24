package com.flatmate.app.match;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/approve")
    public ResponseEntity<Match> approveMatch(@RequestBody Map<String, String> request) {
        String seekerId = request.get("seekerId");
        String ownerId = request.get("ownerId");
        return ResponseEntity.ok(matchService.approveMatch(seekerId, ownerId));
    }

    @PostMapping("/create")
    public ResponseEntity<Match> createMatch(@RequestBody Map<String, String> request) {
        String seekerId = request.get("seekerId");
        String ownerId = request.get("ownerId");
        String listingId = request.getOrDefault("listingId", "test_listing_123");
        return ResponseEntity.ok(matchService.createMatchRequest(seekerId, ownerId, listingId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Match>> getMatches(@PathVariable String userId) {
        return ResponseEntity.ok(matchService.getMyMatches(userId));
    }
}
