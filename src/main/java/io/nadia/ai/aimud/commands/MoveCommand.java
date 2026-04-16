package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public abstract class MoveCommand implements Command {
    protected final CharacterService characterService;
    protected final CommunicationService communicationService;
    protected final RoomService roomService;

    protected abstract Long getNextRoomId(Room currentRoom);

    protected abstract String getDirectionName();

    @Override
    public Mono<Void> execute(Mobile Mobile, String commandLine) {
        log.info("Executing move {} command for Mobile: {} in room id {}.", getDirectionName(), Mobile.getName(), Mobile.getCurrentRoomId());

        return this.roomService.getRoom(Mobile.getCurrentRoomId()).flatMap(room -> {
            Long nextRoomId = getNextRoomId(room);
            if (nextRoomId != null) {
                // Find all followers in the SAME room before the leader moves
                java.util.List<Mobile> followers = characterService.findAllByRoomId(room.getId()).stream()
                        .filter(c -> Mobile.getId().equals(c.getFollowingId()))
                        .toList();

                if (Mobile.isHidden() || Mobile.isInvisible()) {
                    return this.characterService.enterRoom(Mobile, nextRoomId)
                            .then(reactor.core.publisher.Flux.fromIterable(followers)
                                    .flatMap(follower -> {
                                        follower.setFollowingId(null);
                                        communicationService.sendTextMessage(follower, "\n\nYou lost track of " + Mobile.getName() + ".");
                                        return characterService.save(follower);
                                    })
                                    .then()
                            );
                } else {
                    return this.characterService.enterRoom(Mobile, nextRoomId)
                            .then(reactor.core.publisher.Flux.fromIterable(followers)
                                    .flatMap(follower -> {
                                        communicationService.sendTextMessage(follower, "\n\nYou follow " + Mobile.getName() + " " + getDirectionName() + ".");
                                        return characterService.enterRoom(follower, nextRoomId);
                                    })
                                    .then()
                            );
                }
            } else {
                communicationService.sendTextMessage(Mobile, "\n\nYou can't go " + getDirectionName() + " from here.");
                return Mono.empty();
            }
        }).then();
    }

    @Override
    public String getDescription() {
        return "Move " + getDirectionName() + ".";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: " + getDirectionName() + "\n\nMoves your Mobile in the " + getDirectionName() + " direction, if an exit exists.";
    }
}

