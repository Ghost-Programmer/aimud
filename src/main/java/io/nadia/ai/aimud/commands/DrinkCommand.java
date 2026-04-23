package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.ItemService;
import io.nadia.ai.aimud.types.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Optional;
/**
 * DrinkCommand standard implementation layer.
 * Drink liquid from your inventory.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "drink")
public class DrinkCommand implements Command {
    private final CommunicationService communicationService;
    private final MobileService mobileService;
    private final ItemService itemService;

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing drink command for Mobile: {}", mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nDrink what?");
            return Mono.empty();
        }

        if (mobile.getThirst() >= 100) {
            communicationService.sendTextMessage(mobile, "\n\nYou are not thirsty right now.");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();

        Optional<Item> drinkObj = mobile.getInventory().stream()
                .filter(i -> i.getName().toLowerCase().contains(itemName))
                .findFirst();

        if (drinkObj.isEmpty()) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't have that in your inventory.");
            return Mono.empty();
        }

        Item item = drinkObj.get();
        if (item.getItemType() != ItemType.DRINK) {
            communicationService.sendTextMessage(mobile, "\n\nYou can't drink that.");
            return Mono.empty();
        }

        int sips = item.getProperty1();
        int thirstRestored = item.getProperty2();

        mobile.setThirst(Math.min(100, mobile.getThirst() + thirstRestored));
        communicationService.sendTextMessage(mobile, "\n\nYou drink from " + item.getName() + ".");
        communicationService.roomMessage(mobile, "\n" + mobile.getName() + " drinks from " + item.getName() + ".");

        if (sips > 1) {
            item.setProperty1(sips - 1);
            return itemService.saveItem(item).then();
        } else {
            communicationService.sendTextMessage(mobile, "\n\nYou drain the last drops from " + item.getName() + " and discard it.");
            return mobileService.destroyInventoryItem(mobile, item.getId()).then();
        }
    }

    @Override
    public String getDescription() {
        return "Drink liquid from your inventory.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: drink <item>\n\nConsumes liquid from an item in your inventory to replenish your thirst levels. Drinking prevents dehydration and allows health to naturally regenerate.";
    }
}

