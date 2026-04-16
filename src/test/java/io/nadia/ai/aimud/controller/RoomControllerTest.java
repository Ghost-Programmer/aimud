package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.repository.RoomRepository;
import io.nadia.ai.aimud.types.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoomControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RoomRepository roomRepository;

    private Room testRoom;

    @BeforeEach
    void setUp() {
        testRoom = new Room();
        testRoom.setId(1L);
        testRoom.setName("Test Room");
        testRoom.setDescription("Test Description");
        testRoom.setRoomType(RoomType.INDOORS);
    }

    @Test
    void testGetRooms() {
        when(roomRepository.findAll()).thenReturn(Flux.just(testRoom));

        webTestClient.get().uri("/api/rooms")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.rooms[0].name").isEqualTo("Test Room");
    }

    @Test
    void testGetRoomById() {
        when(roomRepository.findById(1L)).thenReturn(Mono.just(testRoom));

        webTestClient.get().uri("/api/rooms/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Test Room");
    }

    @Test
    void testUpdateRoom() {
        Room updatedRoom = new Room();
        updatedRoom.setName("Updated Room");
        updatedRoom.setItems("1,2,3");

        when(roomRepository.findById(1L)).thenReturn(Mono.just(testRoom));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        webTestClient.put().uri("/api/rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedRoom)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Updated Room")
                .jsonPath("$.items").isEqualTo("1,2,3");
    }
}
