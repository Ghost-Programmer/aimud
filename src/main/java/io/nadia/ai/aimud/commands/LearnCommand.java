package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.SkillService;
import io.nadia.ai.aimud.types.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Optional;
/**
 * LearnCommand standard implementation layer.
 * Learn a skill from a book in your inventory.
 */

@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "learn")
public class LearnCommand implements Command {

    private final CommunicationService communicationService;
    private final SkillService skillService;
    private final MobileService mobileService;

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing learn command for Mobile: {}", mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nLearn what?");
            return Mono.empty();
        }

        String itemName = parts[1].trim().toLowerCase();
        Optional<Item> bookToRead = mobile.getInventory().stream()
                .filter(item -> item.getItemType() == ItemType.BOOK)
                .filter(item -> item.getName() != null && item.getName().toLowerCase().contains(itemName))
                .findFirst();

        if (bookToRead.isEmpty()) {
            communicationService.sendTextMessage(mobile, "\n\nYou don't have a book by that name.");
            return Mono.empty();
        }

        Item book = bookToRead.get();
        long skillId = book.getProperty1();
        if (skillId <= 0) {
            communicationService.sendTextMessage(mobile, "\n\nThat book has no valid skill encoded in it.");
            return Mono.empty();
        }

        return skillService.getSkillNameById(skillId)
                .switchIfEmpty(Mono.defer(() -> {
                    communicationService.sendTextMessage(mobile, "\n\nYou cannot decipher this book.");
                    return Mono.empty();
                }))
                .flatMap(skillName -> {
                    if (skillService.getSkillRank(mobile, skillName) > 0) {
                        communicationService.sendTextMessage(mobile, "\n\nYou already know " + skillName + ".");
                        return Mono.empty();
                    }

                    return skillService.addSkill(mobile, skillName)
                            .flatMap(addedSkill -> mobileService.destroyInventoryItem(mobile, book.getId())
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



