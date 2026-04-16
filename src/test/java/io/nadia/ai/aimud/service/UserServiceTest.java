package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.User;
import io.nadia.ai.aimud.repository.MobileRepository;
import io.nadia.ai.aimud.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private MobileRepository mobileRepository;
    @Mock private StatService statService;

    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, mobileRepository, passwordEncoder, jwtService, statService);
    }

    @Test
    void registerUser_FirstUserBecomesAdmin() {
        User user = new User("admin", "secret");
        when(userRepository.findByUsername("admin")).thenReturn(Mono.empty());
        when(userRepository.count()).thenReturn(Mono.just(0L));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(userService.registerUser(user))
                .assertNext(saved -> assertThat(saved.getRole()).isEqualTo("MUD_ADMIN"))
                .verifyComplete();
    }

    @Test
    void registerUser_SubsequentUserGetsMudUserRole() {
        User user = new User("player", "secret");
        when(userRepository.findByUsername("player")).thenReturn(Mono.empty());
        when(userRepository.count()).thenReturn(Mono.just(5L));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(userService.registerUser(user))
                .assertNext(saved -> assertThat(saved.getRole()).isEqualTo("MUD_USER"))
                .verifyComplete();
    }

    @Test
    void registerUser_DuplicateUsernameEmitsError() {
        User existing = new User("taken", "pass");
        when(userRepository.findByUsername("taken")).thenReturn(Mono.just(existing));

        StepVerifier.create(userService.registerUser(new User("taken", "other")))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("Username already exists"))
                .verify();
    }

    @Test
    void registerUser_PasswordIsHashed() {
        String plainPassword = "plaintext";
        User user = new User("hashme", plainPassword);
        when(userRepository.findByUsername("hashme")).thenReturn(Mono.empty());
        when(userRepository.count()).thenReturn(Mono.just(1L));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(userService.registerUser(user))
                .assertNext(saved -> {
                    assertThat(saved.getPassword()).isNotEqualTo(plainPassword);
                    assertThat(passwordEncoder.matches(plainPassword, saved.getPassword())).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void login_ValidCredentialsReturnsJwtToken() {
        String encoded = passwordEncoder.encode("mypassword");
        User stored = new User("jeff", encoded);
        stored.setRole("MUD_USER");
        when(userRepository.findByUsername("jeff")).thenReturn(Mono.just(stored));

        StepVerifier.create(userService.login("jeff", "mypassword"))
                .assertNext(token -> {
                    assertThat(token).isNotBlank();
                    assertThat(jwtService.extractUsername(token)).isEqualTo("jeff");
                })
                .verifyComplete();
    }

    @Test
    void login_WrongPasswordEmitsError() {
        String encoded = passwordEncoder.encode("correct");
        User stored = new User("jeff", encoded);
        when(userRepository.findByUsername("jeff")).thenReturn(Mono.just(stored));

        StepVerifier.create(userService.login("jeff", "wrong"))
                .expectErrorMatches(e -> e.getMessage().contains("Invalid username or password"))
                .verify();
    }

    @Test
    void login_LockedAccountEmitsError() {
        String encoded = passwordEncoder.encode("pass");
        User stored = new User("locked", encoded);
        stored.setLocked(true);
        when(userRepository.findByUsername("locked")).thenReturn(Mono.just(stored));

        StepVerifier.create(userService.login("locked", "pass"))
                .expectErrorMatches(e -> e.getMessage().contains("Account is locked"))
                .verify();
    }

    @Test
    void toggleLock_FlipsLockedState() {
        User user = new User("player", "pass");
        user.setId(1L);
        user.setLocked(false);
        when(userRepository.findById(1L)).thenReturn(Mono.just(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(userService.toggleLock(1L))
                .assertNext(u -> assertThat(u.isLocked()).isTrue())
                .verifyComplete();
    }

    @Test
    void toggleLock_UserNotFoundEmitsError() {
        when(userRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(userService.toggleLock(99L))
                .expectErrorMatches(e -> e.getMessage().contains("User not found"))
                .verify();
    }

    @Test
    void changePassword_UpdatesEncodedPassword() {
        User user = new User("player", "oldencoded");
        user.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Mono.just(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(userService.changePassword(2L, "newPass123"))
                .assertNext(u -> assertThat(passwordEncoder.matches("newPass123", u.getPassword())).isTrue())
                .verifyComplete();
    }

    @Test
    void getAllUsersWithCharacters_ReturnsMappedPayloads() {
        User user = new User("alice", "pw");
        user.setId(10L);
        user.setRole("MUD_USER");
        when(userRepository.findAll()).thenReturn(Flux.just(user));
        when(mobileRepository.findByUserId(10L)).thenReturn(Flux.empty());

        StepVerifier.create(userService.getAllUsersWithCharacters())
                .assertNext(map -> {
                    assertThat(map.get("username")).isEqualTo("alice");
                    assertThat(map.get("role")).isEqualTo("MUD_USER");
                })
                .verifyComplete();
    }
}

