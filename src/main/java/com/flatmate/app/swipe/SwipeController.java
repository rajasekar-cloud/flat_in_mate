package com.flatmate.app.swipe;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

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
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "status", "LIMIT_EXCEEDED"));
        }
    }
}
