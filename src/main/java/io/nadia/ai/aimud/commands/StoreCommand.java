package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.StoreDialogEvent;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;
/**
 * StoreCommand standard implementation layer.
 * Opens the store for a merchant in the room.
 */

@Slf4j
@Component
@MudCommand(name = "store")
@RequiredArgsConstructor
public class StoreCommand implements Command {

    private final MobileService mobileService;
    private final CommunicationService communicationService;

    @Override
    public reactor.core.publisher.Mono<Void> execute(Mobile character, String arguments) {
        Long roomId = character.getCurrentRoomId();
        if (roomId == null) {
            communicationService.sendTextMessage(character, "You are not anywhere.");
            return reactor.core.publisher.Mono.empty();
        }

        List<Mobile> mobilesInRoom = mobileService.getMobilesInRoom(roomId);
        Mobile merchant = mobilesInRoom.stream()
                .peek(m -> log.info("Mobile: {}", m.getName()))
                .filter(m -> !m.getId().equals(character.getId()) && m.getStoreId() != null)
                .findFirst()
                .orElse(null);

        if (merchant == null) {
            communicationService.sendTextMessage(character, "There is no store here.");
            return reactor.core.publisher.Mono.empty();
        }

        StoreDialogEvent event = new StoreDialogEvent(character.getId(), merchant.getStoreId(), merchant.getName());
        communicationService.sendTextMessage(character, "Opening store for " + merchant.getName() + "...");
        communicationService.sendStoreDialog(event);
        return reactor.core.publisher.Mono.empty();
    }

    @Override
    public String getDescription() {
        return "Opens the store for a merchant in the room.";
    }

    @Override
    public String getDetailedDescription() {
        return "store - Opens the store dialog if a shopkeeper is in the current room.";
    }
}
