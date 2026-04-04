package com.aimud.aimud.commands;

import com.aimud.aimud.annontation.MudCommand;
import com.aimud.aimud.model.Item;
import com.aimud.aimud.model.Mobile;
import com.aimud.aimud.service.CharacterService;
import com.aimud.aimud.service.CommunicationService;
import com.aimud.aimud.service.SkillService;
import com.aimud.aimud.types.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "learn")
public class LearnCommand implements Command {

    private final CommunicationService communicationService;
    private final SkillService skillService;
    private final CharacterService characterService;

    @Override
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing learn command for Mobile: {}", Mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(Mobile, "\n\nLearn what?");
            return Mono.empty();
        }

        String itemName = parts[1].trim().toLowerCase();
        Optional<Item> bookToRead = Mobile.getInventory().stream()
                .filter(item -> item.getItemType() == ItemType.BOOK)
                .filter(item -> item.getName() != null && item.getName().toLowerCase().contains(itemName))
                .findFirst();

        if (bookToRead.isEmpty()) {
            communicationService.sendTextMessage(Mobile, "\n\nYou don't have a book by that name.");
            return Mono.empty();
        }

        Item book = bookToRead.get();
        long skillId = book.getProperty1();
        if (skillId <= 0) {
            communicationService.sendTextMessage(Mobile, "\n\nThat book has no valid skill encoded in it.");
            return Mono.empty();
        }

        return skillService.getSkillNameById(skillId)
                .switchIfEmpty(Mono.defer(() -> {
                    communicationService.sendTextMessage(Mobile, "\n\nYou cannot decipher this book.");
                    return Mono.empty();
                }))
                .flatMap(skillName -> {
                    if (skillService.getSkillRank(Mobile, skillName) > 0) {
                        communicationService.sendTextMessage(Mobile, "\n\nYou already know " + skillName + ".");
                        return Mono.empty();
                    }

                    return skillService.addSkill(Mobile, skillName)
                            .flatMap(addedSkill -> characterService.destroyInventoryItem(Mobile, book.getId())
                                    .doOnNext(updatedCharacter -> {
                                        communicationService.sendTextMessage(updatedCharacter,
                                                "\n\nYou study " + book.getName() + " and learn " + addedSkill.getName() + " at rank 1.");
                                        communicationService.sendCharacterUpdate(updatedCharacter);
                                    }))
                            .then();
                });
    }

    @Override
    public String getDescription() {
        return "Learn a skill from a book in your inventory.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: learn <book>\n\nReads a book from your inventory, learns the skill encoded in the book, and destroys the book.";
    }
}


