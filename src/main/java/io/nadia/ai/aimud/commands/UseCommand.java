package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.EffectService;
import io.nadia.ai.aimud.service.ItemService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.SpellService;
import io.nadia.ai.aimud.spells.Spell;
import io.nadia.ai.aimud.types.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "use")
public class UseCommand implements Command {

    private final CommunicationService communicationService;
    private final MobileService mobileService;
    private final ItemService itemService;
    private final SpellService spellService;
    private final EffectService effectService;

    @Override
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing use command for Mobile: {}", mobile.getName());

        String[] parts = commandLine.trim().split("\\s+");
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nUse what?");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();

        return Flux.fromIterable(mobile.getInventory())
                .filter(i -> i.getName().toLowerCase().contains(itemName))
                .next()
                .flatMap(item -> {
                    if (item.getItemType() == ItemType.WAND) {
                        return handleWand(mobile, item, parts);
                    } else if (item.getItemType() == ItemType.POTION) {
                        return handlePotion(mobile, item);
                    } else {
                        communicationService.sendTextMessage(mobile, "\n\nYou cannot use that in this way.");
                        return Mono.empty();
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    communicationService.sendTextMessage(mobile, "\n\nYou are not carrying that.");
                    return Mono.empty();
                }));
    }

    private Mono<Void> handleWand(Mobile mobile, Item wand, String[] parts) {
        if (wand.getProperty3() <= 0) {
            communicationService.sendTextMessage(mobile, "\n\nThe wand fizzles uselessly. It is out of charges.");
            communicationService.roomMessage(mobile, "\n" + mobile.getName() + " waves a wand, but it fizzles uselessly.");
            return Mono.empty();
        }

        Spell spell = spellService.getAllSpells().stream()
                .filter(s -> s.getSpellId() == wand.getProperty1())
                .findFirst()
                .orElse(null);

        if (spell == null) {
            communicationService.sendTextMessage(mobile, "\n\nThe wand glows, but no magic springs forth.");
            return Mono.empty();
        }

        Mobile target = spell.getTarget(mobile, parts);

        if (target == null && spell.requiresTarget()) {
            communicationService.sendTextMessage(mobile, "\n\nYou must specify a valid target to use that wand.");
            return Mono.empty();
        }

        communicationService.sendTextMessage(mobile, "\n\nYou point " + wand.getName() + " and unleash its magic!");
        communicationService.roomMessage(mobile, "\n" + mobile.getName() + " points " + wand.getName() + " and unleashes its magic!");

        spell.cast(mobile, spell, target);

        wand.setProperty3(wand.getProperty3() - 1);

        return itemService.saveItem(wand).then();
    }

    private Mono<Void> handlePotion(Mobile mobile, Item potion) {
        communicationService.sendTextMessage(mobile, "\n\nYou quaff " + potion.getName() + ".");
        communicationService.roomMessage(mobile, "\n" + mobile.getName() + " quaffs " + potion.getName() + ".");

        List<Long> effectIds = new ArrayList<>();
        if (potion.getProperty1() > 0) effectIds.add((long) potion.getProperty1());
        if (potion.getProperty2() > 0) effectIds.add((long) potion.getProperty2());

        int duration = potion.getProperty3() > 0 ? potion.getProperty3() : 10;

        Mono<Void> applyEffectsMono = Flux.fromIterable(effectIds)
                .flatMap(effectService::getEffect)
                .flatMap(effect -> effectService.attachEffectToCharacter(mobile, null, effect, duration, "Potion"))
                .then();

        return applyEffectsMono.then(Mono.defer(() -> {
            List<Item> currentInventory = new ArrayList<>(mobile.getInventory());
            currentInventory.remove(potion);
            mobile.setInventory(currentInventory);
            
            return mobileService.updateInventory(mobile, currentInventory)
                    .then(itemService.deleteItem(potion.getId()));
        }));
    }

    @Override
    public String getDescription() {
        return "Use an item in your inventory, such as a wand or potion.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: use <item> [target]\n\n" +
               "Allows you to consume a potion or invoke the magic stored inside a wand. " +
               "Wands require a target if their embedded spell is offensive.";
    }
}
