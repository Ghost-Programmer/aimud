package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.ServerSettings;
import io.nadia.ai.aimud.repository.ServerSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerSettingsControllerTest {

    @Mock
    private ServerSettingsRepository repository;

    @Test
    void getSettings_ReturnsRepositoryValueWhenPresent() {
        ServerSettingsController controller = new ServerSettingsController(repository);
        ServerSettings settings = new ServerSettings(1L, "Test", true, false, "", null, null, null, null);
        when(repository.findById(1L)).thenReturn(Mono.just(settings));

        StepVerifier.create(controller.getSettings())
                .expectNext(settings)
                .verifyComplete();
    }

    @Test
    void getSettings_ReturnsDefaultWhenMissing() {
        ServerSettingsController controller = new ServerSettingsController(repository);
        when(repository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(controller.getSettings())
                .assertNext(settings -> {
                    assertThat(settings.id()).isEqualTo(1L);
                    assertThat(settings.serverName()).isEqualTo("AI Mud");
                    assertThat(settings.allowNewUser()).isTrue();
                })
                .verifyComplete();
    }
}

