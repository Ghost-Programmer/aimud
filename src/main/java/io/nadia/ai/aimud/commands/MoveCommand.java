package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.Room;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
/**
 * MoveCommand standard implementation layer.
 * Primary processing handler mapping structural integrations natively.
 */

@Slf4j
@RequiredArgsConstructor
public abstract class MoveCommand implements Command {
    
    protected final CommunicationService communicationService;
    protected final RoomService roomService;
    protected final MobileService mobileService;

    protected abstract Long getNextRoomId(Room currentRoom);

    protected abstract String getDirectionName();

    @Override
    /**

     * Execute sequence logic maps.
     * @param Mobile local contextual object
     * @param commandLine trailing standard query parameters
     * @return a reactive pipeline
     */
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing move {} command for Mobile: {} in room id {}.", getDirectionName(), mobile.getName(), mobile.getCurrentRoomId());

        return this.roomService.getRoom(mobile.getCurrentRoomId()).flatMap(room -> {
            Long nextRoomId = getNextRoomId(room);
            if (nextRoomId != null) {
                // Find all followers in the SAME room before the leader moves
                java.util.List<Mobile> followers = mobileService.findAllByRoomId(room.getId()).stream()
                        .filter(c -> mobile.getId().equals(c.getFollowingId()))
                        .toList();

                if (mobile.isHidden() || mobile.isInvisible()) {
                    return this.mobileService.enterRoom(mobile, nextRoomId)
                            .then(reactor.core.publisher.Flux.fromIterable(followers)
                                    .flatMap(follower -> {
                                        follower.setFollowingId(null);
                                        communicationService.sendTextMessage(follower, "\n\nYou lost track of " + mobile.getName() + ".");
                                        return mobileService.save(follower);
                                    })
                                    .then()
                            );
                } else {
                    return this.mobileService.enterRoom(mobile, nextRoomId)
                            .then(reactor.core.publisher.Flux.fromIterable(followers)
                                    .flatMap(follower -> {
                                        communicationService.sendTextMessage(follower, "\n\nYou follow " + mobile.getName() + " " + getDirectionName() + ".");
                                        return mobileService.enterRoom(follower, nextRoomId);
                                    })
                                    .then()
                            );
                }
            } else {
                communicationService.sendTextMessage(mobile, "\n\nYou can't go " + getDirectionName() + " from here.");
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


