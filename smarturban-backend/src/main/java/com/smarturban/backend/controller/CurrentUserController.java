package com.smarturban.backend.controller;

import com.smarturban.backend.entity.User;
import com.smarturban.backend.security.UserDetailsImpl;
import com.smarturban.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CurrentUserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("fullName", user.getFullName());
        profile.put("email", user.getEmail());
        profile.put("phone", user.getPhone());
        profile.put("address", user.getAddress());
        profile.put("role", user.getRole());
        profile.put("fcmToken", user.getFcmToken());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @RequestBody Map<String, String> request) {
        User updated = userService.updateProfile(
                userDetails.getId(),
                request.get("fullName"),
                request.get("phone"),
                request.get("address")
        );
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<?> registerFcmToken(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @RequestBody Map<String, String> request) {
        String token = request.get("fcmToken");
        User user = userService.findByEmail(userDetails.getUsername());
        user.setFcmToken(token);
        userService.saveUser(user);
        return ResponseEntity.ok(Map.of("message", "FCM device token registered successfully"));
    }
}
