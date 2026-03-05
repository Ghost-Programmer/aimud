package com.aimud.aimud.controller;

import com.aimud.aimud.model.User;
import com.aimud.aimud.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<Object>> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of("message", "Username is required")));
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of("message", "Password is required")));
        }

        return userRepository.findByUsername(user.getUsername())
                .flatMap(existingUser -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body((Object)Map.of("message", "Username already exists"))))
                .switchIfEmpty(Mono.defer(() -> {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    return userRepository.save(user).map(savedUser -> ResponseEntity.ok((Object)savedUser));
                }));
    }
}
