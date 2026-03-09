package com.aimud.aimud.controller;

import com.aimud.aimud.model.Room;
import com.aimud.aimud.model.enums.RoomType;
import com.aimud.aimud.repository.RoomRepository;
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
    void testCreateRoom() {
        when(roomRepository.save(any(Room.class))).thenReturn(Mono.just(testRoom));

        webTestClient.post().uri("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testRoom)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Test Room");
    }
}
