package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.types.ItemType;
import io.nadia.ai.aimud.types.WearLocation;
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

    @Mock
    private MobileService mobileService;

    @Mock
    private ConfigService configService;

    @Mock
    private StoreService storeService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private McpToolService mcpToolService;

    @BeforeEach
    void setUp() {
        mcpToolService = new McpToolService(roomService, itemService, effectService, mobileService, configService,
                storeService, tokenBlacklistService);
    }

    @Test
    void createItemMapsToPersistableEnums() {
        when(itemService.saveItem(any(Item.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Item savedItem = mcpToolService.createItem(
                "Chain Shirt",
                "A simple protective shirt made from linked rings.",
                "LIGHT_ARMOR",
                "CHEST",
                null,
                null,
                null,
                null,
                null,
                null);

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
                "PRIMARY",
                false,
                11,
                22,
                33,
                44,
                null))
                .subscribeOn(Schedulers.parallel()))
                .assertNext(savedItem -> {
                    assertThat(savedItem.getName()).isEqualTo("Vorpal Blade");
                    assertThat(savedItem.getItemType()).isEqualTo(ItemType.WEAPON);
                    assertThat(savedItem.getWearLocation()).isEqualTo(WearLocation.PRIMARY);
                    assertThat(savedItem.getProperty1()).isEqualTo(11);
                    assertThat(savedItem.getProperty2()).isEqualTo(22);
                    assertThat(savedItem.getProperty3()).isEqualTo(33);
                    assertThat(savedItem.getProperty4()).isEqualTo(44);
                })
                .verifyComplete();
    }

    @Test
    void createItemAcceptsBookItemType() {
        when(itemService.saveItem(any(Item.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Item savedItem = mcpToolService.createItem(
                "Book of Lore",
                "A dusty tome containing old guild techniques.",
                "Book",
                "PRIMARY",
                null,
                null,
                null,
                null,
                null,
                null);

        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getItemType()).isEqualTo(ItemType.BOOK);
        assertThat(savedItem.getWearLocation()).isEqualTo(WearLocation.PRIMARY);
        assertThat(savedItem.getProperty1()).isZero();
        assertThat(savedItem.getProperty2()).isZero();
        assertThat(savedItem.getProperty3()).isZero();
        assertThat(savedItem.getProperty4()).isZero();
    }

    @Test
    void updateItemPreservesExistingEffectsWhenSaving() {
        Item existingItem = new Item();
        existingItem.setId(42L);
        existingItem.setName("Old Sword");
        existingItem.setDescription("Old description");
        existingItem.setItemType(ItemType.WEAPON);
        existingItem.setWearLocation(WearLocation.PRIMARY);
        existingItem.setProperty1(1);
        existingItem.setProperty2(2);
        existingItem.setProperty3(3);
        existingItem.setProperty4(4);
        Effect effect = new Effect();
        existingItem.setEffects(List.of(effect));

        when(itemService.getItem(42L)).thenReturn(Mono.just(existingItem));
        when(itemService.saveItem(any(Item.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Item savedItem = mcpToolService.updateItem(
                42L,
                "New Sword",
                "New description",
                "WEAPON",
                "PRIMARY",
                null,
                101,
                102,
                null,
                null,
                null);

        assertThat(savedItem).isNotNull();
        assertThat(savedItem.getName()).isEqualTo("New Sword");
        assertThat(savedItem.getDescription()).isEqualTo("New description");
        assertThat(savedItem.getProperty1()).isEqualTo(101);
        assertThat(savedItem.getProperty2()).isEqualTo(102);
        assertThat(savedItem.getProperty3()).isEqualTo(3);
        assertThat(savedItem.getProperty4()).isEqualTo(4);
        assertThat(savedItem.getEffects()).containsExactly(effect);
        verify(itemService).getItem(42L);
        verify(itemService).saveItem(existingItem);
    }

    @Test
    void toolMetadataSmokeTestStillReturnsLists() {
        when(itemService.getAllItems()).thenReturn(Flux.empty());
        when(roomService.getAllRooms()).thenReturn(Flux.empty());

        assertThat(mcpToolService.listItems()).isEmpty();
        assertThat(mcpToolService.listRooms()).isEmpty();
    }

    @Test
    void updateRoomSetsDirectionalDoorFieldsWhenExitProvided() {
        Room source = new Room();
        source.setId(10L);
        source.setName("Hall");

        when(roomService.getRoom(10L)).thenReturn(Mono.just(source));
        when(roomService.saveRoom(any(Room.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Room updated = mcpToolService.updateRoom(10L, null, null, null, 11L, null, null, null, null, null);

        assertThat(updated.getNorthId()).isEqualTo(11L);
        assertThat(updated.isNorthDoor()).isTrue();
        verify(roomService).saveRoom(source);
    }
}
