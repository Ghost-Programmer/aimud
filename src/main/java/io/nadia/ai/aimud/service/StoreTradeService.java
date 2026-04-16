package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.StoreItem;
import io.nadia.ai.aimud.model.StorePayloads.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreTradeService {

    private final StoreService storeService;
    private final CharacterService characterService;
    private final FactionService factionService;
    private final CommunicationService communicationService;
    private final ItemService itemService;
    private final MobileService mobileService;

    public int calculateBuyPrice(Item item, int factionRating, int charisma) {
        int baseValue = item.getValue() > 0 ? item.getValue() : itemService.calculateItemValue(item);
        double factionModifier = (50 - factionRating) / 50.0 * 0.25;
        double charismaModifier = (250 - charisma) / 250.0 * 0.10;
        double totalModifier = factionModifier + charismaModifier;
        return Math.max(1, (int) (baseValue * (1.0 + totalModifier)));
    }

    public int calculateSellPrice(Item item, int factionRating, int charisma) {
        int baseValue = item.getValue() > 0 ? item.getValue() : itemService.calculateItemValue(item);
        double factionModifier = (50 - factionRating) / 50.0 * 0.25;
        double charismaModifier = (250 - charisma) / 250.0 * 0.10;
        double totalModifier = factionModifier + charismaModifier;
        // Strict 40% cap multiplied by the inverse of the markup to prevent exploiting
        int baseSellPrice = (int) (baseValue * 0.40);
        return Math.max(1, (int) (baseSellPrice * (1.0 - totalModifier)));
    }

    private Mobile findMerchant(Long storeId, Mobile character) {
        if (character.getCurrentRoomId() == null)
            return null;
        return mobileService.getMobilesInRoom(character.getCurrentRoomId()).stream()
                .filter(m -> storeId.equals(m.getStoreId()))
                .findFirst()
                .orElse(null);
    }

    public Mono<StoreDialogPayload> getStoreDialogPayload(Long storeId, Long characterId) {
        return characterService.getCharacterById(characterId).flatMap(character -> {
            Mobile merchant = findMerchant(storeId, character);
            if (merchant == null)
                return Mono.empty();

            int factionRating = factionService.getFactionRatingSync(character, merchant.getFactionId());
            int charisma = character.getCurrentCharisma() > 0 ? character.getCurrentCharisma() : 250;

            return storeService.getStore(storeId).map(store -> {
                List<StoreItemDTO> storeItems = (store.getItems() != null ? store.getItems()
                        : new ArrayList<StoreItem>())
                        .stream()
                        .map(si -> new StoreItemDTO(si.getItemId(), si.getItem(), si.getAvailable(),
                                calculateBuyPrice(si.getItem(), factionRating, charisma)))
                        .collect(Collectors.toList());

                List<PlayerItemDTO> playerItems = new ArrayList<>();
                if (character.getInventory() != null) {
                    for (Item item : character.getInventory()) {
                        playerItems.add(new PlayerItemDTO(item.getId(), item,
                                calculateSellPrice(item, factionRating, charisma)));
                    }
                }

                return new StoreDialogPayload(storeId, merchant.getName(), factionRating, charisma, character.getGold(),
                        storeItems, playerItems);
            });
        });
    }

    public Mono<StoreDialogPayload> buyItem(Long storeId, Long itemId, Long characterId) {
        return characterService.getCharacterById(characterId).flatMap(character -> {
            Mobile merchant = findMerchant(storeId, character);
            if (merchant == null)
                return Mono.empty();

            int factionRating = factionService.getFactionRatingSync(character, merchant.getFactionId());
            int charisma = character.getCurrentCharisma() > 0 ? character.getCurrentCharisma() : 250;

            return storeService.getStore(storeId).flatMap(store -> {
                StoreItem storeItem = store.getItems().stream()
                        .filter(si -> si.getItemId().equals(itemId))
                        .findFirst()
                        .orElse(null);

                if (storeItem == null || (storeItem.getAvailable() == 0)) {
                    communicationService.sendTextMessage(character, "They are out of stock.");
                    return getStoreDialogPayload(storeId, characterId);
                }

                int price = calculateBuyPrice(storeItem.getItem(), factionRating, charisma);
                if (character.getGold() < price) {
                    communicationService.sendTextMessage(character, "You do not have enough gold.");
                    return getStoreDialogPayload(storeId, characterId);
                }

                // Process transaction
                character.setGold(character.getGold() - price);
                if (storeItem.getAvailable() > 0) {
                    storeItem.setAvailable(storeItem.getAvailable() - 1);
                    if (storeItem.getAvailable() == 0) {
                        store.getItems().remove(storeItem);
                    }
                }

                // Add to inventory
                List<Item> currentInventory = new ArrayList<>(character.getInventory());
                Item boughtItem = storeItem.getItem();
                
                boolean itemAdded = false;
                if (boughtItem.isStackable()) {
                    for (Item invItem : currentInventory) {
                        if (invItem.getId().equals(boughtItem.getId())) {
                            invItem.setCount(invItem.getCount() + 1);
                            itemAdded = true;
                            break;
                        }
                    }
                }

                if (!itemAdded) {
                    Item newItem = new Item();
                    newItem.setId(boughtItem.getId());
                    newItem.setItemType(boughtItem.getItemType());
                    newItem.setWearLocation(boughtItem.getWearLocation());
                    newItem.setNoPickup(boughtItem.isNoPickup());
                    newItem.setStackable(boughtItem.isStackable());
                    newItem.setCount(1);
                    newItem.setName(boughtItem.getName());
                    newItem.setDescription(boughtItem.getDescription());
                    newItem.setProperty1(boughtItem.getProperty1());
                    newItem.setProperty2(boughtItem.getProperty2());
                    newItem.setProperty3(boughtItem.getProperty3());
                    newItem.setProperty4(boughtItem.getProperty4());
                    newItem.setEffects(boughtItem.getEffects());
                    newItem.setValue(boughtItem.getValue());
                    currentInventory.add(newItem);
                }
                
                character.setInventory(currentInventory);

                return characterService.save(character)
                        .flatMap(savedChar -> characterService.updateInventory(savedChar, currentInventory))
                        .flatMap(savedChar -> characterService.getCharacterById(savedChar.getId()))
                        .flatMap(savedChar -> {
                            communicationService.sendCharacterUpdate(savedChar);
                            communicationService.sendTextMessage(savedChar,
                                    "You bought " + storeItem.getItem().getName() + " for " + price + " gold.");
                            return getStoreDialogPayload(storeId, characterId);
                        });
            });
        });
    }

    public Mono<StoreDialogPayload> sellItem(Long storeId, Long itemId, Long characterId) {
        return characterService.getCharacterById(characterId).flatMap(character -> {
            Mobile merchant = findMerchant(storeId, character);
            if (merchant == null)
                return Mono.empty();

            int factionRating = factionService.getFactionRatingSync(character, merchant.getFactionId());
            int charisma = character.getCurrentCharisma() > 0 ? character.getCurrentCharisma() : 250;

            Item itemToSell = character.getInventory().stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);

            if (itemToSell == null) {
                communicationService.sendTextMessage(character, "You do not have that item.");
                return getStoreDialogPayload(storeId, characterId);
            }

            return storeService.getStore(storeId).flatMap(store -> {
                int unitPrice = calculateSellPrice(itemToSell, factionRating, charisma);
                int totalQuantity = itemToSell.isStackable() ? itemToSell.getCount() : 1;
                int price = unitPrice * totalQuantity;

                // Process transaction
                character.setGold(character.getGold() + price);
                List<Item> currentInventory = new ArrayList<>(character.getInventory());
                currentInventory.remove(itemToSell);
                character.setInventory(currentInventory);

                StoreItem existingStoreItem = store.getItems().stream()
                        .filter(si -> si.getItemId().equals(itemId))
                        .findFirst()
                        .orElse(null);

                if (existingStoreItem != null) {
                    if (existingStoreItem.getAvailable() > 0) {
                        existingStoreItem.setAvailable(existingStoreItem.getAvailable() + totalQuantity);
                    }
                } else {
                    StoreItem newTransientSi = new StoreItem();
                    newTransientSi.setStoreId(storeId);
                    newTransientSi.setItemId(itemId);
                    newTransientSi.setItem(itemToSell);
                    newTransientSi.setAvailable(totalQuantity);
                    store.getItems().add(newTransientSi);
                }

                return characterService.save(character)
                        .flatMap(savedChar -> characterService.updateInventory(savedChar, currentInventory))
                        .flatMap(savedChar -> characterService.getCharacterById(savedChar.getId()))
                        .flatMap(savedChar -> {
                            communicationService.sendCharacterUpdate(savedChar);
                            communicationService.sendTextMessage(savedChar,
                                    "You sold " + itemToSell.getName() + " for " + price + " gold.");
                            return getStoreDialogPayload(storeId, characterId);
                        });
            });
        });
    }
}
