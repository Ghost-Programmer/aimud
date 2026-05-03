package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.CharacterClass;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.Race;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.ConfigService;
import io.nadia.ai.aimud.service.MobileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * WhoCommand standard implementation layer.
 * Lists all active players currently in the MUD.
 */
@Slf4j
@RequiredArgsConstructor
@MudCommand(name = "who")
public class WhoCommand implements Command {
    private final CommunicationService communicationService;
    private final MobileService mobileService;
    private final ConfigService configService;

    @Override
    public Mono<Void> execute(Mobile mobile, String commandLine) {
        log.info("Executing who command for Mobile: {}", mobile.getName());

        List<Mobile> players = mobileService.getAvailableCharacters().stream()
                .filter(m -> m.getUserId() != null)
                .collect(Collectors.toList());

        if (players.isEmpty()) {
            communicationService.sendTextMessage(mobile, "\n\nThere are no players currently online.");
            return Mono.empty();
        }

        communicationService.sendTextMessage(mobile, "\n\n--- Active Players ---");

        return Flux.fromIterable(players)
                .flatMap(player -> {
                    Mono<String> raceNameMono = player.getRaceId() != null
                            ? configService.getAllRaces().filter(r -> r.getId().equals(player.getRaceId())).next()
                                    .map(Race::getName).defaultIfEmpty("Unknown Race")
                            : Mono.just("Unknown Race");

                    Mono<String> classNameMono = player.getClassId() != null
                            ? configService.getAllCharacterClasses().filter(c -> c.getId().equals(player.getClassId()))
                                    .next().map(CharacterClass::getName).defaultIfEmpty("Unknown Class")
                            : Mono.just("Unknown Class");

                    return Mono.zip(raceNameMono, classNameMono).map(tuple -> {
                        String raceName = tuple.getT1();
                        String className = tuple.getT2();
                        int level = (int) player.getChallengeRating();
                        return String.format("\n[%2d %s %s] %s", level, raceName, className, player.getName());
                    });
                })
                .collectList()
                .flatMap(lines -> {
                    for (String line : lines) {
                        communicationService.sendTextMessage(mobile, line);
                    }
                    communicationService.sendTextMessage(mobile,
                            "\n----------------------\nTotal Players: " + lines.size() + "\n");
                    return Mono.empty();
                });
    }

    @Override
    public String getDescription() {
        return "Lists all players currently online.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: who\n\nDisplays a list of all active players in the game along with their level, race, and class.";
    }
}
