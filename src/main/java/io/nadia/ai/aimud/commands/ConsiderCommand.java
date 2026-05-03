package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
@MudCommand(name = "con")
public class ConsiderCommand implements Command {
    private final CommunicationService communicationService;
    private final RoomService roomService;
    private final MobileService mobileService;

    @Override
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing consider command for Mobile: {}", mobile.getName());

        String[] parts = commandLine.trim().split("\\s+", 2);
        if (parts.length < 2) {
            communicationService.sendTextMessage(mobile, "\n\nConsider whom?");
            return Mono.empty();
        }

        String targetName = parts[1].toLowerCase();

        return roomService.getRoom(mobile.getCurrentRoomId())
                .flatMap(room -> {
                    // Find target in room
                    List<Mobile> mobilesInRoom = mobileService.findAllByRoomId(room.getId());
                    Mobile target = mobilesInRoom.stream()
                            .filter(c -> !c.getId().equals(mobile.getId()) && c.getName().toLowerCase().contains(targetName))
                            .findFirst()
                            .orElse(null);

                    if (target == null) {
                        communicationService.sendTextMessage(mobile, "\n\nThey aren't here.");
                        return Mono.empty();
                    }

                    int diff = (int) mobile.getChallengeRating() - (int) target.getChallengeRating();
                    String msg;

                    if (diff > 5) {
                        msg = "You would crush them!";
                    } else if (diff > 0) {
                        msg = "You have the upper hand.";
                    } else if (diff == 0) {
                        msg = "A perfect match.";
                    } else if (diff >= -5) {
                        msg = "They look a bit tougher than you.";
                    } else {
                        msg = "Death awaits you.";
                    }

                    communicationService.sendTextMessage(mobile, "\n\n" + msg);
                    
                    return Mono.empty();
                }).then();
    }

    @Override
    public String getDescription() {
        return "Compare your level to a target.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: consider <target>\n        con <target>\n\nEvaluates the relative strength of a target compared to yourself.";
    }
}
