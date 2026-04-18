package be.gilmotech.brain.users;

import be.gilmotech.brain.users.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<UserProfileDto>> listUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<?> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User requester = requireAdmin(authHeader);
        if (requester == null) return ResponseEntity.status(403).body(Map.of("error", "Admin requis"));
        try {
            return ResponseEntity.ok(userService.createUser(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable String userId,
            @RequestBody UpdateUserRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User requester = requireAuth(authHeader);
        if (requester == null) return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
        if (!requester.getId().equals(userId) && !"admin".equals(requester.getRole())) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès refusé"));
        }
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @PostMapping("/{userId}/avatar")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User requester = requireAuth(authHeader);
        if (requester == null) return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
        if (!requester.getId().equals(userId) && !"admin".equals(requester.getRole())) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès refusé"));
        }
        try {
            String url = userService.uploadAvatar(userId, file);
            return ResponseEntity.ok(Map.of("avatarUrl", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable String userId,
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User requester = requireAuth(authHeader);
        if (requester == null) return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
        if (!requester.getId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès refusé"));
        }
        try {
            userService.changePassword(userId, request);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deactivateUser(
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User requester = requireAdmin(authHeader);
        if (requester == null) return ResponseEntity.status(403).body(Map.of("error", "Admin requis"));
        try {
            userService.deactivateUser(userId);
            return ResponseEntity.ok(Map.of("deactivated", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private User requireAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try { return authService.validateToken(authHeader.substring(7)); }
        catch (Exception e) { return null; }
    }

    private User requireAdmin(String authHeader) {
        User user = requireAuth(authHeader);
        return (user != null && "admin".equals(user.getRole())) ? user : null;
    }
}
