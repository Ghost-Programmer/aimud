package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.User;
import io.nadia.ai.aimud.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<Object>> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of("message", "Username is required")));
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of("message", "Password is required")));
        }

        return userService.registerUser(user)
                .map(savedUser -> ResponseEntity.ok((Object) savedUser))
                .onErrorResume(IllegalArgumentException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()))));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Object>> login(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of("message", "Username is required")));
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of("message", "Password is required")));
        }

        return userService.login(user.getUsername(), user.getPassword())
                .map(token -> ResponseEntity.ok((Object) Map.of("token", token)))
                .onErrorResume(RuntimeException.class, e -> {
                    if (e.getMessage().equals("Account is locked")) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Account is locked")));
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials")));
                });
    }

    @GetMapping
    public Flux<Map<String, Object>> getAllUsers() {
        return userService.getAllUsersWithCharacters();
    }

    @PutMapping("/{userId}/password")
    public Mono<ResponseEntity<Object>> changePassword(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        String newPassword = body.get("password");
        if (newPassword == null || newPassword.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of("message", "Password is required")));
        }

        return userService.changePassword(userId, newPassword)
                .map(updatedUser -> ResponseEntity.ok((Object) updatedUser))
                .onErrorResume(RuntimeException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()))));
    }

    @PutMapping("/{userId}/lock")
    public Mono<ResponseEntity<Object>> toggleLock(@PathVariable Long userId) {
        return userService.toggleLock(userId)
                .map(updatedUser -> ResponseEntity.ok((Object) updatedUser))
                .onErrorResume(RuntimeException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()))));
    }
}
