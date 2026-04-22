package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.types.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "put")
public class PutCommand implements Command {
    private final CharacterService characterService;
    private final CommunicationService communicationService;

    @Override
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing put command for Mobile: {}", Mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 4);
        if (parts.length < 4 || !parts[2].equalsIgnoreCase("in")) {
            communicationService.sendTextMessage(Mobile, "\n\nSyntax: put <item> in <container>");
            return Mono.empty();
        }

        String itemName = parts[1].toLowerCase();
        String containerName = parts[3].toLowerCase();

        Optional<Item> itemToPutOpt = Mobile.getInventory().stream()
                .filter(i -> i.getName().toLowerCase().contains(itemName))
                .findFirst();

        if (itemToPutOpt.isEmpty()) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't have that item in your inventory.");
            return Mono.empty();
        }

        Item itemToPut = itemToPutOpt.get();

        Optional<Item> containerOpt = Mobile.getInventory().stream()
                .filter(i -> i.getName().toLowerCase().contains(containerName))
                .findFirst();

        if (containerOpt.isEmpty()) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't have that container in your inventory.");
            return Mono.empty();
        }

        Item container = containerOpt.get();

        if (container.getItemType() != ItemType.CONTAINER) {
            communicationService.sendTextMessage(Mobile, "\n\n" + container.getName() + " is not a container.");
            return Mono.empty();
        }

        if (itemToPut.getId().equals(container.getId())) {
            communicationService.sendTextMessage(Mobile, "\n\nYou cannot put a container inside itself.");
            return Mono.empty();
        }

        if (itemToPut.getItemType() == ItemType.CONTAINER) {
            communicationService.sendTextMessage(Mobile, "\n\nYou cannot put a container inside another container.");
            return Mono.empty();
        }

        // Check container capacity if needed
        int maxItems = container.getProperty2();

        if (container.getInventory() == null) {
            container.setInventory(new ArrayList<>());
        }

        if (maxItems > 0 && container.getInventory().size() >= maxItems) {
            communicationService.sendTextMessage(Mobile, "\n\n" + container.getName() + " is full.");
            return Mono.empty();
        }

        // Move item
        Mobile.getInventory().remove(itemToPut);
        container.getInventory().add(itemToPut);

        communicationService.sendTextMessage(Mobile, "\n\nYou put " + itemToPut.getName() + " in " + container.getName() + ".");

        return characterService.updateInventory(Mobile, Mobile.getInventory()).then();
    }

    @Override
    public String getDescription() {
        return "Put an item into a container.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: put <item> in <container>\n\nMoves an item from your main inventory into a container you are carrying.";
    }
}
