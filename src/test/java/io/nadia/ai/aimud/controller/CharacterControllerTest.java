package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.MobileMacro;
import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CharacterControllerTest {

    @Mock
    private MobileService MobileService;

    @Mock
    private RoomService roomService;

    @Test
    void selectCharacter_ReturnsOk() {
        CharacterController controller = new CharacterController(MobileService, roomService);
        when(MobileService.selectCharacter(1L)).thenReturn(Mono.empty());

        StepVerifier.create(controller.selectCharacter(1L))
                .assertNext(resp -> assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK))
                .verifyComplete();

        verify(MobileService).selectCharacter(1L);
    }

    @Test
    void getAvailableCharacters_ReturnsFluxFromServiceList() {
        CharacterController controller = new CharacterController(MobileService, roomService);
        Mobile m = new Mobile();
        m.setId(1L);
        when(MobileService.getAvailableCharacters()).thenReturn(List.of(m));

        StepVerifier.create(controller.getAvailableCharacters())
                .expectNext(m)
                .verifyComplete();
    }

    @Test
    void createCharacter_UsesUsernameFromReactiveSecurityContext() {
        CharacterController controller = new CharacterController(MobileService, roomService);
        Mobile request = new Mobile();
        request.setName("Hero");
        Mobile created = new Mobile();
        created.setName("Hero");

        when(MobileService.createCharacter("jeff", request)).thenReturn(Mono.just(created));

        Authentication auth = new TestingAuthenticationToken("jeff", "pw");
        StepVerifier.create(controller.createCharacter(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)))
                .assertNext(resp -> {
                    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(resp.getBody()).isEqualTo(created);
                })
                .verifyComplete();

        verify(MobileService).createCharacter("jeff", request);
    }

    @Test
    void getCharacter_NotFoundMapsTo404() {
        CharacterController controller = new CharacterController(MobileService, roomService);
        when(MobileService.getCharacterById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(controller.getCharacter(10L))
                .assertNext(resp -> assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                .verifyComplete();
    }

    @Test
    void getCharacterRoom_ReturnsRoomWhenCharacterAndRoomExist() {
        CharacterController controller = new CharacterController(MobileService, roomService);
        Mobile character = new Mobile();
        character.setId(3L);
        character.setCurrentRoomId(8L);
        Room room = new Room();
        room.setId(8L);
        room.setName("Square");

        when(MobileService.getCharacterById(3L)).thenReturn(Mono.just(character));
        when(roomService.getRoom(8L)).thenReturn(Mono.just(room));

        StepVerifier.create(controller.getCharacterRoom(3L))
                .assertNext(resp -> {
                    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(resp.getBody()).isEqualTo(room);
                })
                .verifyComplete();
    }

    @Test
    void saveMacros_DelegatesToService() {
        CharacterController controller = new CharacterController(MobileService, roomService);
        MobileMacro macro = new MobileMacro();
        macro.setLabel("A");
        List<MobileMacro> macros = List.of(macro);
        when(MobileService.saveCharacterMacros(5L, macros)).thenReturn(Flux.fromIterable(macros));

        StepVerifier.create(controller.saveMacros(5L, macros))
                .expectNext(macro)
                .verifyComplete();

        verify(MobileService).saveCharacterMacros(5L, macros);
    }
}

