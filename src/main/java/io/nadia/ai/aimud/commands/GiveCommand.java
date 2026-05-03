package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
@MudCommand(name = "give")
public class GiveCommand implements Command {
    private final CommunicationService communicationService;
    private final MobileService mobileService;
    private final RoomService roomService;

    @Override
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing give command for Mobile: {}", mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 3);
        if (parts.length < 3) {
            communicationService.sendTextMessage(mobile, "\n\nSyntax: give <item|amount> <player>");
            return Mono.empty();
        }

        String amountOrItem = parts[1].toLowerCase();
        String targetName = parts[2].toLowerCase();

        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    List<Mobile> charactersInRoom = mobileService.findAllByRoomId(room.getId());
                    Mobile target = charactersInRoom.stream()
                            .filter(c -> !c.getId().equals(mobile.getId()) && c.getName().toLowerCase().contains(targetName))
                            .findFirst()
                            .orElse(null);

                    if (target == null) {
                        communicationService.sendTextMessage(mobile, "\n\nThey aren't here.");
                        return Mono.empty();
                    }

                    // Check if giving gold
                    if (amountOrItem.matches("\\d+")) {
                        int amount = Integer.parseInt(amountOrItem);
                        if (amount <= 0) {
                            communicationService.sendTextMessage(mobile, "\n\nYou must give a positive amount.");
                            return Mono.empty();
                        }
                        if (mobile.getGold() < amount) {
                            communicationService.sendTextMessage(mobile, "\n\nYou don't have that much gold.");
                            return Mono.empty();
                        }

                        mobile.setGold(mobile.getGold() - amount);
                        target.setGold(target.getGold() + amount);

                        communicationService.sendTextMessage(mobile, "\n\nYou give " + amount + " gold to " + target.getName() + ".");
                        if (target.getUserId() != null) {
                            communicationService.sendTextMessage(target, "\n\n" + mobile.getName() + " gives you " + amount + " gold.");
                        }

                        return Mono.when(
                                target.getUserId() != null ? mobileService.save(target) : Mono.empty(),
                                mobile.getUserId() != null ? mobileService.save(mobile) : Mono.empty()
                        ).then();
                    }

                    // Handle giving an item
                    Optional<Item> itemOpt = mobile.getInventory().stream()
                            .filter(i -> i.getName().toLowerCase().contains(amountOrItem))
                            .findFirst();

                    if (itemOpt.isEmpty()) {
                        communicationService.sendTextMessage(mobile, "\n\nYou don't have that item.");
                        return Mono.empty();
                    }

                    Item itemToGive = itemOpt.get();

                    mobile.getInventory().remove(itemToGive);
                    target.getInventory().add(itemToGive);

                    communicationService.sendTextMessage(mobile, "\n\nYou give " + itemToGive.getName() + " to " + target.getName() + ".");
                    if (target.getUserId() != null) {
                        communicationService.sendTextMessage(target, "\n\n" + mobile.getName() + " gives you " + itemToGive.getName() + ".");
                    }

                    return Mono.when(
                            mobileService.updateInventory(mobile, mobile.getInventory()),
                            mobileService.updateInventory(target, target.getInventory())
                    ).then();
                }).then();
    }

    @Override
    public String getDescription() {
        return "Transfer an item or gold to another person.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: give <item> <player>\n        give <amount> <player>\n\nGives an item from your inventory or a specified amount of gold to someone in the same room.";
    }
}
