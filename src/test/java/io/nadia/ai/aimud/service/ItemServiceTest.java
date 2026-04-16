package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Effect;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.repository.ItemRepository;
import io.nadia.ai.aimud.types.EffectType;
import io.nadia.ai.aimud.types.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private EffectService effectService;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService(itemRepository, effectService);
    }

    // --- calculateItemValue ---

    @Test
    void calculateItemValue_NullItemReturnsZero() {
        assertThat(itemService.calculateItemValue(null)).isZero();
    }

    @Test
    void calculateItemValue_ItemWithNoEffectsReturnsBaseValue() {
        Item item = new Item();
        item.setEffects(List.of());
        assertThat(itemService.calculateItemValue(item)).isEqualTo(50);
    }

    @Test
    void calculateItemValue_StrengthEffectAdds100PerPoint() {
        Item item = new Item();
        Effect e = new Effect(EffectType.STRENGTH, 3, 0, 0, 0);
        item.setEffects(List.of(e));
        // base 50 + 3 * 100 = 350
        assertThat(itemService.calculateItemValue(item)).isEqualTo(350);
    }

    @Test
    void calculateItemValue_ArmorEffectAdds50PerPoint() {
        Item item = new Item();
        Effect e = new Effect(EffectType.ARMOR, 4, 0, 0, 0);
        item.setEffects(List.of(e));
        // base 50 + 4 * 50 = 250
        assertThat(itemService.calculateItemValue(item)).isEqualTo(250);
    }

    @Test
    void calculateItemValue_FlyEffectAdds1000() {
        Item item = new Item();
        Effect e = new Effect(EffectType.FLY, 0, 0, 0, 0);
        item.setEffects(List.of(e));
        // base 50 + 1000 = 1050
        assertThat(itemService.calculateItemValue(item)).isEqualTo(1050);
    }

    @Test
    void calculateItemValue_NegativeModifierReducesValueByHalf() {
        Item item = new Item();
        Effect e = new Effect(EffectType.STRENGTH, -2, 0, 0, 0);
        item.setEffects(List.of(e));
        // base 50 - (2 * 100 / 2) = 50 - 100 = clamped to min 1
        assertThat(itemService.calculateItemValue(item)).isGreaterThanOrEqualTo(1);
    }

    @Test
    void calculateItemValue_MultipleEffectsAreAccumulated() {
        Item item = new Item();
        Effect str = new Effect(EffectType.STRENGTH, 1, 0, 0, 0);    // +100
        Effect armor = new Effect(EffectType.ARMOR, 2, 0, 0, 0);     // +100
        item.setEffects(List.of(str, armor));
        // base 50 + 100 + 100 = 250
        assertThat(itemService.calculateItemValue(item)).isEqualTo(250);
    }

    // --- saveItem ---

    @Test
    void saveItem_WithNullEffectsSavesItemDirectly() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Dagger");
        item.setEffects(null);

        when(itemRepository.save(item)).thenReturn(Mono.just(item));

        StepVerifier.create(itemService.saveItem(item))
                .assertNext(saved -> assertThat(saved.getName()).isEqualTo("Dagger"))
                .verifyComplete();
    }

    @Test
    void saveItem_WithEffectsDeletesOldAndLinksNew() {
        Item item = new Item();
        item.setId(5L);
        item.setName("Magic Sword");
        Effect effect = new Effect();
        effect.setId(99L);
        effect.setEffectType(EffectType.STRENGTH);
        effect.setModifier1(3);
        item.setEffects(List.of(effect));

        when(itemRepository.save(item)).thenReturn(Mono.just(item));
        when(effectService.deleteByItemId(5L)).thenReturn(Mono.empty());
        when(effectService.linkItemAndEffect(eq(5L), eq(99L))).thenReturn(Mono.empty());
        when(effectService.getEffect(99L)).thenReturn(Mono.just(effect));

        StepVerifier.create(itemService.saveItem(item))
                .assertNext(saved -> {
                    assertThat(saved.getName()).isEqualTo("Magic Sword");
                    assertThat(saved.getEffects()).hasSize(1);
                })
                .verifyComplete();

        verify(effectService).deleteByItemId(5L);
        verify(effectService).linkItemAndEffect(5L, 99L);
    }

    // --- deleteItem ---

    @Test
    void deleteItem_DeletesEffectsAndThenItem() {
        when(effectService.deleteByItemId(10L)).thenReturn(Mono.empty());
        when(itemRepository.deleteById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(itemService.deleteItem(10L))
                .verifyComplete();

        verify(effectService).deleteByItemId(10L);
        verify(itemRepository).deleteById(10L);
    }
}

