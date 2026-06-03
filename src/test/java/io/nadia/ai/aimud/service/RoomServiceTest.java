package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.*;
import io.nadia.ai.aimud.repository.RoomRepository;
import io.nadia.ai.aimud.types.EffectType;
import io.nadia.ai.aimud.types.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests verifying room visibility and light source calculation rules
 * within the {@link RoomService}.
 */
@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private MobileService mobileService;
    @Mock
    private ConfigService configService;
    @Mock
    private ItemService itemService;

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService(roomRepository, mobileService, configService, itemService);
    }

    /**
     * Verifies that day and night ambient light settings are respected correctly.
     */
    @Test
    void calculateCurrentLightValue_ShouldCalculateAmbientLightBasedOnDayNight() {
        // Arrange
        Room room = new Room();
        room.setId(1L);
        room.setDayLightValue(5);
        room.setNightLightValue(2);

        ServerSettings daySettings = new ServerSettings(1L, "Test Server", true, false, "", 12, 1, 1, 2026, null, null, "system", "system");
        ServerSettings nightSettings = new ServerSettings(1L, "Test Server", true, false, "", 0, 1, 1, 2026, null, null, "system", "system");

        // Test Day
        when(configService.getServerSettings()).thenReturn(Mono.just(daySettings));
        StepVerifier.create(roomService.calculateCurrentLightValue(room))
                .expectNext(5)
                .verifyComplete();
        assertThat(room.getCurrentLightValue()).isEqualTo(5);

        // Test Night
        when(configService.getServerSettings()).thenReturn(Mono.just(nightSettings));
        StepVerifier.create(roomService.calculateCurrentLightValue(room))
                .expectNext(2)
                .verifyComplete();
        assertThat(room.getCurrentLightValue()).isEqualTo(2);
    }

    /**
     * Verifies that transient items of type LIGHT laying in the room add their brightness value.
     */
    @Test
    void calculateCurrentLightValue_ShouldAddTransientLightSourceBrightness() {
        // Arrange
        Room room = new Room();
        room.setId(1L);
        room.setDayLightValue(1);

        ServerSettings daySettings = new ServerSettings(1L, "Test Server", true, false, "", 12, 1, 1, 2026, null, null, "system", "system");
        when(configService.getServerSettings()).thenReturn(Mono.just(daySettings));

        // Create transient light item
        Item torch = new Item();
        torch.setItemType(ItemType.LIGHT);
        torch.setProperty1(3); // Brightness 3

        roomService.addTransientItemToRoom(1L, torch);

        // Act & Assert
        StepVerifier.create(roomService.calculateCurrentLightValue(room))
                .expectNext(4) // 1 ambient + 3 torch brightness
                .verifyComplete();
        assertThat(room.getCurrentLightValue()).isEqualTo(4);
    }

    /**
     * Verifies that equipped light items carried by any mobile in the room add their brightness value.
     */
    @Test
    void calculateCurrentLightValue_ShouldAddEquippedLightSourceBrightness() {
        // Arrange
        Room room = new Room();
        room.setId(1L);
        room.setDayLightValue(1);

        ServerSettings daySettings = new ServerSettings(1L, "Test Server", true, false, "", 12, 1, 1, 2026, null, null, "system", "system");
        when(configService.getServerSettings()).thenReturn(Mono.just(daySettings));

        // Mock mobile present in the room holding a light source in their offhand slot
        Mobile player = new Mobile();
        player.setCurrentRoomId(1L);
        Item magicLantern = new Item();
        magicLantern.setItemType(ItemType.LIGHT);
        magicLantern.setProperty1(4); // Brightness 4
        player.setOffhand(magicLantern);

        List<Mobile> mobiles = new ArrayList<>();
        mobiles.add(player);
        when(mobileService.findAllByRoomId(1L)).thenReturn(mobiles);

        // Act & Assert
        StepVerifier.create(roomService.calculateCurrentLightValue(room))
                .expectNext(5) // 1 ambient + 4 lantern brightness
                .verifyComplete();
        assertThat(room.getCurrentLightValue()).isEqualTo(5);
    }

    /**
     * Verifies that database-persisted light items on the ground add their brightness value.
     */
    @Test
    void calculateCurrentLightValue_ShouldAddPersistedLightSourceBrightness() {
        // Arrange
        Room room = new Room();
        room.setId(1L);
        room.setDayLightValue(1);
        room.setItems("100,200"); // Persisted item IDs

        ServerSettings daySettings = new ServerSettings(1L, "Test Server", true, false, "", 12, 1, 1, 2026, null, null, "system", "system");
        when(configService.getServerSettings()).thenReturn(Mono.just(daySettings));

        Item nonLightItem = new Item();
        nonLightItem.setItemType(ItemType.WEAPON);

        Item persistedLight = new Item();
        persistedLight.setItemType(ItemType.LIGHT);
        persistedLight.setProperty1(5); // Brightness 5

        when(itemService.getItem(100L)).thenReturn(Mono.just(nonLightItem));
        when(itemService.getItem(200L)).thenReturn(Mono.just(persistedLight));

        // Act & Assert
        StepVerifier.create(roomService.calculateCurrentLightValue(room))
                .expectNext(6) // 1 ambient + 5 light source brightness
                .verifyComplete();
        assertThat(room.getCurrentLightValue()).isEqualTo(6);
    }

    /**
     * Verifies that room spell effects (darkvision, darkness) modify room light level appropriately.
     */
    @Test
    void calculateCurrentLightValue_ShouldEvaluateSpellEffects() {
        // Arrange
        Room room = new Room();
        room.setId(1L);
        room.setDayLightValue(4);

        ServerSettings daySettings = new ServerSettings(1L, "Test Server", true, false, "", 12, 1, 1, 2026, null, null, "system", "system");
        when(configService.getServerSettings()).thenReturn(Mono.just(daySettings));

        // Add a darkness effect
        Effect darknessEffect = new Effect();
        darknessEffect.setEffectType(EffectType.DARKNESS);
        darknessEffect.setModifier1(3); // Reduces light by 3

        CharacterEffect ce = new CharacterEffect();
        ce.setEffect(darknessEffect);

        List<CharacterEffect> effects = new ArrayList<>();
        effects.add(ce);
        room.setEffects(effects);

        // Act & Assert
        StepVerifier.create(roomService.calculateCurrentLightValue(room))
                .expectNext(1) // 4 ambient - 3 darkness
                .verifyComplete();
        assertThat(room.getCurrentLightValue()).isEqualTo(1);
    }
}
