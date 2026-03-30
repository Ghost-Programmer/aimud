package com.aimud.aimud.service;

import com.aimud.aimud.model.User;
import com.aimud.aimud.repository.MobileRepository;
import com.aimud.aimud.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MobileRepository mobileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StatService statService;

    public UserService(UserRepository userRepository, MobileRepository mobileRepository, PasswordEncoder passwordEncoder, JwtService jwtService, StatService statService) {
        this.userRepository = userRepository;
        this.mobileRepository = mobileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.statService = statService;
    }

    public Mono<User> registerUser(User user) {
        return userRepository.findByUsername(user.getUsername())
                .flatMap(existingUser -> Mono.<User>error(new IllegalArgumentException("Username already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    return userRepository.count()
                            .flatMap(count -> {
                                if (count == 0) {
                                    user.setRole("MUD_ADMIN");
                                } else {
                                    user.setRole("MUD_USER");
                                }
                                return userRepository.save(user);
                            });
                }));
    }

    public Mono<String> login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .flatMap(user -> {
                    if (user.isLocked()) {
                        return Mono.error(new RuntimeException("Account is locked"));
                    }
                    return Mono.just(jwtService.generateToken(user.getUsername(), user.getRole()));
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid username or password")));
    }

    public Flux<Map<String, Object>> getAllUsersWithCharacters() {
        return userRepository.findAll()
                .flatMap(user -> mobileRepository.findByUserId(user.getId())
                        .flatMap(statService::updateCurrentStats)
                        .collectList()
                        .map(characters -> Map.of(
                                "id", user.getId(),
                                "username", user.getUsername(),
                                "role", user.getRole(),
                                "locked", user.isLocked(),
                                "characters", characters
                        )));
    }

    public Mono<User> changePassword(Long userId, String newPassword) {
        return userRepository.findById(userId)
                .flatMap(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    return userRepository.save(user);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }

    public Mono<User> toggleLock(Long userId) {
        return userRepository.findById(userId)
                .flatMap(user -> {
                    user.setLocked(!user.isLocked());
                    return userRepository.save(user);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }
}
