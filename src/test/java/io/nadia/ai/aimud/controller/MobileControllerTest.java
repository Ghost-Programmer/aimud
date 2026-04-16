package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.MobileAction;
import io.nadia.ai.aimud.service.MobileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileControllerTest {

    @Mock
    private MobileService mobileService;

    @Test
    void createMobile_DelegatesToSave() {
        MobileController controller = new MobileController(mobileService);
        Mobile mobile = new Mobile();
        mobile.setName("Goblin");
        when(mobileService.saveMobile(mobile)).thenReturn(Mono.just(mobile));

        StepVerifier.create(controller.createMobile(mobile))
                .expectNext(mobile)
                .verifyComplete();

        verify(mobileService).saveMobile(mobile);
    }

    @Test
    void updateMobile_SetsPathIdBeforeSaving() {
        MobileController controller = new MobileController(mobileService);
        Mobile mobile = new Mobile();
        when(mobileService.saveMobile(mobile)).thenReturn(Mono.just(mobile));

        StepVerifier.create(controller.updateMobile(99L, mobile))
                .assertNext(saved -> assertThat(saved.getId()).isEqualTo(99L))
                .verifyComplete();

        verify(mobileService).saveMobile(mobile);
    }

    @Test
    void saveActions_DelegatesToService() {
        MobileController controller = new MobileController(mobileService);
        MobileAction action = new MobileAction();
        action.setActionCommand("attack");
        List<MobileAction> actions = List.of(action);
        when(mobileService.saveMobileActions(5L, actions)).thenReturn(Flux.fromIterable(actions));

        StepVerifier.create(controller.saveActions(5L, actions))
                .expectNext(action)
                .verifyComplete();

        verify(mobileService).saveMobileActions(5L, actions);
    }
}

