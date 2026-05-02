package com.flatmate.app.listing;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    // ── Create (Publish) — validates min 5 photos ────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Listing listing) {
        try {
            return ResponseEntity.ok(listingService.createListing(listing));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Save as Draft — no photo validation, partial save allowed ────────────
    @PostMapping("/draft")
    public ResponseEntity<Listing> saveDraft(@RequestBody Listing listing) {
        return ResponseEntity.ok(listingService.saveDraft(listing));
    }

    // ── Update existing listing ──────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Listing listing) {
        try {
            return ResponseEntity.ok(listingService.updateListing(id, listing));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── Deactivate listing (soft delete) ────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deactivate(@PathVariable String id) {
        listingService.deactivateListing(id);
        return ResponseEntity.ok("Listing deactivated successfully");
    }

    // ── Get all published listings ───────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Listing>> getAll() {
        return ResponseEntity.ok(listingService.getAllListings());
    }

    // ── Get listing by ID ────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Listing> get(@PathVariable String id) {
        return ResponseEntity.ok(listingService.getListing(id));
    }

    // ── Get all listings by owner ────────────────────────────────────────────
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Listing>> getByOwner(@PathVariable String ownerId) {
        return ResponseEntity.ok(listingService.getListingsByOwner(ownerId));
    }

    // ── Get S3 pre-signed URL for photo/video upload ─────────────────────────
    // Supports: .jpg .png .heic .mp4 .mov etc.
    @GetMapping("/{id}/upload-url")
    public ResponseEntity<Map<String, String>> getUploadUrl(
            @PathVariable String id,
            @RequestParam String fileName) {
        String url = listingService.generateUploadUrl("listings/" + id + "/" + fileName);
        return ResponseEntity.ok(Map.of("url", url, "filePath", "listings/" + id + "/" + fileName));
    }

    // ── Get multiple S3 pre-signed URLs in a single call ─────────────────────
    @PostMapping("/{id}/bulk-upload-urls")
    public ResponseEntity<List<Map<String, String>>> getBulkUploadUrls(
            @PathVariable String id,
            @RequestBody List<String> fileNames) {
        List<Map<String, String>> response = fileNames.stream()
                .map(fileName -> {
                    String filePath = "listings/" + id + "/" + fileName;
                    String url = listingService.generateUploadUrl(filePath);
                    return Map.of(
                            "fileName", fileName,
                            "url", url,
                            "filePath", filePath
                    );
                })
                .toList();
        return ResponseEntity.ok(response);
    }
}
