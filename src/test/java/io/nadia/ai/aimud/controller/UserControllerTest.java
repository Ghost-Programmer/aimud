package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.User;
import io.nadia.ai.aimud.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Test
    void register_ValidUserReturnsOk() {
        UserController controller = new UserController(userService);
        User input = new User("alice", "secret");
        when(userService.registerUser(input)).thenReturn(Mono.just(input));

        StepVerifier.create(controller.register(input))
                .assertNext(resp -> {
                    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(resp.getBody()).isEqualTo(input);
                })
                .verifyComplete();
    }

    @Test
    void register_MissingUsernameReturnsBadRequest() {
        UserController controller = new UserController(userService);
        User input = new User();
        input.setPassword("secret");

        StepVerifier.create(controller.register(input))
                .assertNext(resp -> {
                    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(resp.getBody()).isEqualTo(Map.of("message", "Username is required"));
                })
                .verifyComplete();
    }

    @Test
    void login_LockedAccountMapsToForbidden() {
        UserController controller = new UserController(userService);
        User input = new User("alice", "secret");
        when(userService.login("alice", "secret")).thenReturn(Mono.error(new RuntimeException("Account is locked")));

        StepVerifier.create(controller.login(input))
                .assertNext(resp -> {
                    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(resp.getBody()).isEqualTo(Map.of("message", "Account is locked"));
                })
                .verifyComplete();
    }

    @Test
    void changePassword_EmptyBodyPasswordReturnsBadRequest() {
        UserController controller = new UserController(userService);

        StepVerifier.create(controller.changePassword(1L, Map.of()))
                .assertNext(resp -> assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST))
                .verifyComplete();
    }

    @Test
    void toggleLock_UserNotFoundMapsToNotFound() {
        UserController controller = new UserController(userService);
        when(userService.toggleLock(3L)).thenReturn(Mono.error(new RuntimeException("User not found")));

        StepVerifier.create(controller.toggleLock(3L))
                .assertNext(resp -> assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                .verifyComplete();
    }
}

