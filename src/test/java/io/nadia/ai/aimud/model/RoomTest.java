package io.nadia.ai.aimud.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoomTest {

    @Test
    void getItemIds_ReturnsEmptyListForNullOrEmpty() {
        Room room = new Room();

        room.setItems(null);
        assertThat(room.getItemIds()).isEmpty();

        room.setItems("");
        assertThat(room.getItemIds()).isEmpty();
    }

    @Test
    void getItemIds_ParsesCommaSeparatedIdsAndTrimsWhitespace() {
        Room room = new Room();
        room.setItems("1, 2,3 , 4");

        assertThat(room.getItemIds()).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    void getItemIds_IgnoresInvalidTokensAndPreservesValidOrder() {
        Room room = new Room();
        room.setItems("11, nope, 22, x, 33");

        assertThat(room.getItemIds()).containsExactly(11L, 22L, 33L);
    }

    @Test
    void getItemIds_PreservesDuplicateValues() {
        Room room = new Room();
        room.setItems("8,8,9");

        assertThat(room.getItemIds()).containsExactly(8L, 8L, 9L);
    }

    @Test
    void getMobileIds_ReturnsEmptyListForNullOrEmpty() {
        Room room = new Room();

        room.setMobiles(null);
        assertThat(room.getMobileIds()).isEmpty();

        room.setMobiles("");
        assertThat(room.getMobileIds()).isEmpty();
    }

    @Test
    void getMobileIds_ParsesCommaSeparatedIdsAndTrimsWhitespace() {
        Room room = new Room();
        room.setMobiles("101, 202,303 ");

        assertThat(room.getMobileIds()).containsExactly(101L, 202L, 303L);
    }

    @Test
    void getMobileIds_IgnoresInvalidTokensAndPreservesValidOrder() {
        Room room = new Room();
        room.setMobiles("5, bad, 6, nope, 7");

        assertThat(room.getMobileIds()).containsExactly(5L, 6L, 7L);
    }

    @Test
    void getMobileIds_PreservesDuplicateValues() {
        Room room = new Room();
        room.setMobiles("4,4,5");

        assertThat(room.getMobileIds()).containsExactly(4L, 4L, 5L);
    }
}

