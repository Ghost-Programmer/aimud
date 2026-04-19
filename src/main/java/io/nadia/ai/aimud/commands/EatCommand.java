package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.ItemService;
import io.nadia.ai.aimud.types.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "eat")
public class EatCommand implements Command {
    private final CommunicationService communicationService;
    private final CharacterService characterService;
    private final ItemService itemService;

    @Override
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing eat command for Mobile: {}", mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nEat what?");
            return Mono.empty();
        }

        if (mobile.getHunger() >= 100) {
            communicationService.sendTextMessage(mobile, "\n\nYou are too full to eat right now.");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();

        Optional<Item> foodObj = mobile.getInventory().stream()
                .filter(i -> i.getName().toLowerCase().contains(itemName))
                .findFirst();

        if (foodObj.isEmpty()) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't have that in your inventory.");
            return Mono.empty();
        }

        Item item = foodObj.get();
        if (item.getItemType() != ItemType.FOOD) {
            communicationService.sendTextMessage(mobile, "\n\nYou can't eat that.");
            return Mono.empty();
        }

        int portions = item.getProperty1();
        int hungerRestored = item.getProperty2();

        mobile.setHunger(Math.min(100, mobile.getHunger() + hungerRestored));
        communicationService.sendTextMessage(mobile, "\n\nYou eat " + item.getName() + ".");
        communicationService.roomMessage(mobile, "\n" + mobile.getName() + " eats " + item.getName() + ".");

        if (portions > 1) {
            item.setProperty1(portions - 1);
            return itemService.saveItem(item).then();
        } else {
            return characterService.destroyInventoryItem(mobile, item.getId()).then();
        }
    }

    @Override
    public String getDescription() {
        return "Eat food from your inventory.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: eat <item>\n\nConsumes a food item from your inventory to replenish your hunger levels. Eating prevents starvation and allows health to naturally regenerate.";
    }
}
