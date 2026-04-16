package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.service.EffectService;
import io.nadia.ai.aimud.types.EffectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EffectControllerTest {

    @Mock
    private EffectService effectService;

    @Test
    void getEffect_NotFoundMapsTo404() {
        EffectController controller = new EffectController(effectService);
        when(effectService.getEffect(1L)).thenReturn(Mono.empty());

        StepVerifier.create(controller.getEffect(1L))
                .assertNext(resp -> assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                .verifyComplete();
    }

    @Test
    void createEffect_DelegatesToService() {
        EffectController controller = new EffectController(effectService);
        Effect effect = new Effect();
        effect.setEffectType(EffectType.STRENGTH);
        when(effectService.saveEffect(effect)).thenReturn(Mono.just(effect));

        StepVerifier.create(controller.createEffect(effect))
                .assertNext(resp -> {
                    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(resp.getBody()).isEqualTo(effect);
                })
                .verifyComplete();

        verify(effectService).saveEffect(effect);
    }

    @Test
    void getEffects_BuildsPagedResponse() {
        EffectController controller = new EffectController(effectService);
        Effect e1 = new Effect();
        e1.setId(1L);
        e1.setName("A");
        e1.setEffectType(EffectType.STRENGTH);
        when(effectService.getAllEffects()).thenReturn(Flux.just(e1));

        StepVerifier.create(controller.getEffects(0, 10, null, null, "id"))
                .assertNext(map -> {
                    assertThat(map).containsKeys("effects", "total", "page", "size");
                    assertThat(map.get("total")).isEqualTo(1);
                })
                .verifyComplete();
    }
}

