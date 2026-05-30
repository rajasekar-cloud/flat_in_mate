package com.flatmate.app.moderation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationRepository moderationRepository;

    @PostMapping("/block")
    public ResponseEntity<Block> blockUser(@RequestBody Map<String, String> request) {
        Block block = Block.builder()
                .blockerId(request.get("blockerId"))
                .blockedId(request.get("blockedId"))
                .createdAt(LocalDateTime.now().toString())
                .build();
        return ResponseEntity.ok(moderationRepository.blockUser(block));
    }

    @DeleteMapping("/block")
    public ResponseEntity<Void> unblockUser(@RequestParam String blockerId, @RequestParam String blockedId) {
        moderationRepository.unblockUser(blockerId, blockedId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/blocks/{userId}")
    public ResponseEntity<List<String>> getBlocks(@PathVariable String userId) {
        return ResponseEntity.ok(moderationRepository.getBlockedUserIds(userId));
    }

    @PostMapping("/report")
    public ResponseEntity<Report> report(@RequestBody Report report) {
        if (report.getReportId() == null) {
            report.setReportId(UUID.randomUUID().toString());
        }
        report.setStatus("PENDING");
        report.setCreatedAt(LocalDateTime.now().toString());
        return ResponseEntity.ok(moderationRepository.saveReport(report));
    }
}
