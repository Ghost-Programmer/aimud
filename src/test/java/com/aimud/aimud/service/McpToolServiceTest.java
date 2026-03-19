package com.aimud.aimud.service;

import com.aimud.aimud.model.Effect;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.Room;
import com.aimud.aimud.types.ItemType;
import com.aimud.aimud.types.WearLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpToolServiceTest {

    @Mock
    private RoomService roomService;

    @Mock
    private ItemService itemService;

    @Mock
    private EffectService effectService;

    private McpToolService mcpToolService;

    @BeforeEach
    void setUp() {
        mcpToolService = new McpToolService(roomService, itemService, effectService);
    }

    @Test
    void createItemMapsAiAliasesToPersistableEnums() {
        when(itemService.saveItem(any(Item.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Item savedItem = mcpToolService.createItem(
                "Chain Shirt",
                "A simple protective shirt made from linked rings.",
                "ARMOR",
                "TORSO"
        );

        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getItemType()).isEqualTo(ItemType.LIGHT_ARMOR);
        assertThat(savedItem.getWearLocation()).isEqualTo(WearLocation.CHEST);
        verify(itemService).saveItem(any(Item.class));
    }

    @Test
    void createItemSucceedsFromNonBlockingThread() {
        when(itemService.saveItem(any(Item.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(Mono.fromCallable(() -> mcpToolService.createItem(
                        "Vorpal Blade",
                        "A powerful sword with the ability to cleave through armor.",
                        "WEAPON",
                        "PRIMARY"
                ))
                .subscribeOn(Schedulers.parallel()))
                .assertNext(savedItem -> {
                    assertThat(savedItem.getName()).isEqualTo("Vorpal Blade");
                    assertThat(savedItem.getItemType()).isEqualTo(ItemType.WEAPON);
                    assertThat(savedItem.getWearLocation()).isEqualTo(WearLocation.PRIMARY);
                })
                .verifyComplete();
    }

    @Test
    void updateItemPreservesExistingEffectsWhenSaving() {
        Item existingItem = new Item();
        existingItem.setId(42L);
        existingItem.setName("Old Sword");
        existingItem.setDescription("Old description");
        existingItem.setItemType(ItemType.WEAPON);
        existingItem.setWearLocation(WearLocation.PRIMARY);
        Effect effect = new Effect();
        existingItem.setEffects(List.of(effect));

        when(itemService.getItem(42L)).thenReturn(Mono.just(existingItem));
        when(itemService.saveItem(any(Item.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Item savedItem = mcpToolService.updateItem(
                42L,
                "New Sword",
                "New description",
                "WEAPON",
                "PRIMARY"
        );

        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getName()).isEqualTo("New Sword");
        assertThat(savedItem.getDescription()).isEqualTo("New description");
        assertThat(savedItem.getEffects()).containsExactly(effect);
        verify(itemService).getItem(42L);
        verify(itemService).saveItem(existingItem);
    }

    @Test
    void toolMetadataSmokeTestStillReturnsLists() {
        when(itemService.getAllItems()).thenReturn(Flux.empty());
        when(roomService.getAllRooms()).thenReturn(Flux.empty());

        assertThat(mcpToolService.getAllItems()).isEmpty();
        assertThat(mcpToolService.getAllRooms()).isEmpty();
    }

    @Test
    void setRoomDoorUpdatesDirectionalDoorFields() {
        Room source = new Room();
        source.setId(10L);
        source.setName("Hall");

        Room destination = new Room();
        destination.setId(11L);
        destination.setName("Armory");

        when(roomService.getRoom(10L)).thenReturn(Mono.just(source));
        when(roomService.getRoom(11L)).thenReturn(Mono.just(destination));
        when(roomService.saveRoom(any(Room.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Room updated = mcpToolService.setRoomDoor(10L, "n", 11L, true);

        assertThat(updated.getNorthId()).isEqualTo(11L);
        assertThat(updated.isNorthDoor()).isTrue();
        assertThat(updated.isNorthDoorOpen()).isTrue();
        verify(roomService).saveRoom(source);
    }

    @Test
    void setRoomDoorRejectsInvalidDirection() {
        assertThatThrownBy(() -> mcpToolService.setRoomDoor(10L, "sideways", 11L, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid direction");
    }
}
