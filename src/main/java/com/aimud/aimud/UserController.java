package com.aimud.aimud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
                .switchIfEmpty(userRepository.save(user).map(savedUser -> ResponseEntity.ok((Object)savedUser)));
    }
}
