package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.types.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StoreTradeServiceTest {

    @Mock private StoreService storeService;
    @Mock private FactionService factionService;
    @Mock private CommunicationService communicationService;
    @Mock private ItemService itemService;
    @Mock private MobileService mobileService;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private StoreTradeService storeTradeService;

    @BeforeEach
    void setUp() {
        storeTradeService = new StoreTradeService(storeService, mobileService, factionService,
                communicationService, itemService, eventPublisher);
    }

    private Item itemWithValue(int value) {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setItemType(ItemType.MISC);
        item.setValue(value);
        return item;
    }

    // --- calculateBuyPrice ---

    @Test
    void calculateBuyPrice_NeutralFactionAndAverageCharismaReturnsInflatedPrice() {
        Item item = itemWithValue(100);
        // factionRating=50, charisma=250 → modifiers = 0 + 0 = 0 → price = 100
        int price = storeTradeService.calculateBuyPrice(item, 50, 250);
        assertThat(price).isEqualTo(100);
    }

    @Test
    void calculateBuyPrice_LowFactionIncreasesPrice() {
        Item item = itemWithValue(100);
        int friendly = storeTradeService.calculateBuyPrice(item, 50, 250);
        int hostile = storeTradeService.calculateBuyPrice(item, 0, 250);
        assertThat(hostile).isGreaterThan(friendly);
    }

    @Test
    void calculateBuyPrice_HighFactionDecreasesPrice() {
        Item item = itemWithValue(100);
        int neutral = storeTradeService.calculateBuyPrice(item, 50, 250);
        int allied = storeTradeService.calculateBuyPrice(item, 100, 250);
        // factionModifier = (50 - 100)/50.0 * 0.25 = -0.25 → price reduced
        assertThat(allied).isLessThan(neutral);
    }

    @Test
    void calculateBuyPrice_HighCharismaDecreasesPrice() {
        Item item = itemWithValue(100);
        int lowCha = storeTradeService.calculateBuyPrice(item, 50, 0);
        int highCha = storeTradeService.calculateBuyPrice(item, 50, 250);
        assertThat(highCha).isLessThanOrEqualTo(lowCha);
    }

    @Test
    void calculateBuyPrice_NeverFallsBelowOne() {
        Item item = itemWithValue(1);
        int price = storeTradeService.calculateBuyPrice(item, 100, 250);
        assertThat(price).isGreaterThanOrEqualTo(1);
    }

    // --- calculateSellPrice ---

    @Test
    void calculateSellPrice_IsAlwaysLessThanBuyPrice() {
        Item item = itemWithValue(200);
        int buy = storeTradeService.calculateBuyPrice(item, 50, 250);
        int sell = storeTradeService.calculateSellPrice(item, 50, 250);
        assertThat(sell).isLessThan(buy);
    }

    @Test
    void calculateSellPrice_IsApproximately40PercentOfBaseValue() {
        Item item = itemWithValue(100);
        // factionRating=50, charisma=250 → totalModifier=0 → sell = 40 * (1 - 0) = 40
        int price = storeTradeService.calculateSellPrice(item, 50, 250);
        assertThat(price).isEqualTo(40);
    }

    @Test
    void calculateSellPrice_NeverFallsBelowOne() {
        Item item = itemWithValue(1);
        int price = storeTradeService.calculateSellPrice(item, 0, 0);
        assertThat(price).isGreaterThanOrEqualTo(1);
    }

    @Test
    void calculateSellPrice_HighFactionIncreasesPayment() {
        Item item = itemWithValue(100);
        int hostile = storeTradeService.calculateSellPrice(item, 0, 250);
        int allied = storeTradeService.calculateSellPrice(item, 100, 250);
        assertThat(allied).isGreaterThan(hostile);
    }
}

