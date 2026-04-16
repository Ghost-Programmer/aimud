package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.User;
import io.nadia.ai.aimud.repository.MobileRepository;
import io.nadia.ai.aimud.repository.UserRepository;
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

    /**
     * Constructs a new UserService.
     *
     * @param userRepository   the user repository
     * @param mobileRepository the mobile repository
     * @param passwordEncoder  the password encoder
     * @param jwtService       the JWT service
     * @param statService      the stat service
     */
    public UserService(UserRepository userRepository, MobileRepository mobileRepository, PasswordEncoder passwordEncoder, JwtService jwtService, StatService statService) {
        this.userRepository = userRepository;
        this.mobileRepository = mobileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.statService = statService;
    }

    /**
     * Registers a newly created user account, ensuring the username is unique
     * and hashing the password. Allocates the first user the MUD_ADMIN role.
     *
     * @param user the user data submitted for registration
     * @return a {@link Mono} emitting the registered user
     */
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

    /**
     * Authenticates a user's credentials against the database and returns a JWT if valid.
     *
     * @param username the submitted username
     * @param password the submitted plaintext password
     * @return a {@link Mono} containing the newly generated JWT token string
     */
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

    /**
     * Retrieves a list of all server users along with their associated and calculated character arrays.
     *
     * @return a {@link Flux} emitting a detailed map of user and character information for each account
     */
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

    /**
     * Updates an existing user's password.
     *
     * @param userId      the internal ID of the user
     * @param newPassword the new plaintext password to encode and save
     * @return a {@link Mono} containing the modified user
     */
    public Mono<User> changePassword(Long userId, String newPassword) {
        return userRepository.findById(userId)
                .flatMap(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    return userRepository.save(user);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }

    /**
     * Toggles the locked status of a user account.
     *
     * @param userId the internal ID of the user
     * @return a {@link Mono} containing the toggled user
     */
    public Mono<User> toggleLock(Long userId) {
        return userRepository.findById(userId)
                .flatMap(user -> {
                    user.setLocked(!user.isLocked());
                    return userRepository.save(user);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }
}
