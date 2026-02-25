package com.aimud.aimud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AppUserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<Map<String, String>>> register(@RequestBody RegisterRequest request) {
        return userRepository.findByEmail(request.email())
                .flatMap(existingUser -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Email already in use"))))
                .switchIfEmpty(
                        userRepository.count().flatMap(count -> {
                            String role = count == 0 ? Role.ADMIN.name() : Role.PLAYER.name();
                            AppUser newUser = new AppUser(
                                    null,
                                    request.email(),
                                    passwordEncoder.encode(request.password()),
                                    role);
                            return userRepository.save(newUser)
                                    .map(savedUser -> ResponseEntity
                                            .ok(Map.of("message", "User registered successfully")));
                        }));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody LoginRequest request) {
        return userRepository.findByEmail(request.email())
                .filter(user -> passwordEncoder.matches(request.password(), user.passwordHash()))
                .map(user -> {
                    String token = jwtService.generateToken(user);
                    return ResponseEntity.ok(Map.of("token", token));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid credentials"))));
    }
}

record RegisterRequest(String email, String password) {
}

record LoginRequest(String email, String password) {
}
