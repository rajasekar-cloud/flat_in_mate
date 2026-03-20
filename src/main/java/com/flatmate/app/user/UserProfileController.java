package com.flatmate.app.user;

import com.flatmate.app.auth.AuthService;
import com.flatmate.app.auth.RoleChangeHistory;
import com.flatmate.app.auth.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final AuthService authService;

    @GetMapping("/{userId}")
    public ResponseEntity<User> getProfile(@PathVariable String userId) {
        return ResponseEntity.ok(userProfileService.getProfile(userId));
    }

    @PostMapping
    public ResponseEntity<?> updateProfile(@RequestBody User profile) {
        try {
            userProfileService.updateProfile(profile);
            return ResponseEntity.ok("Profile updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{userId}/owner")
    public ResponseEntity<?> registerAsOwner(@PathVariable String userId) {
        try {
            userProfileService.registerAsOwner(userId);
            return ResponseEntity.ok("User registered as Owner successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Returns full history of all role switches for this user
    @GetMapping("/{userId}/role-history")
    public ResponseEntity<List<RoleChangeHistory>> getRoleHistory(@PathVariable String userId) {
        return ResponseEntity.ok(authService.getRoleHistory(userId));
    }
}
