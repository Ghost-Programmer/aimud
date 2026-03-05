package com.aimud.aimud.controller;

import com.aimud.aimud.model.User;
import com.aimud.aimud.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
                .map(savedUser -> ResponseEntity.ok((Object)savedUser))
                .onErrorResume(IllegalArgumentException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()))));
    }
}
