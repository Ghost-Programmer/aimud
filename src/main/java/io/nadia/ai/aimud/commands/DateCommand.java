package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.annontation.MudCommand;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.ConfigService;
import org.springframework.context.ApplicationContext;
import reactor.core.publisher.Mono;

@MudCommand(name = "date")
public class DateCommand implements Command {

    private final CommunicationService communicationService;
    private final ConfigService configService;

    public DateCommand(ApplicationContext context) {
        this.communicationService = context.getBean(CommunicationService.class);
        this.configService = context.getBean(ConfigService.class);
    }

    @Override
    public Mono<Void> execute(Mobile mobile, String arguments) {
        return configService.getServerSettings()
                .doOnNext(settings -> {
                    String phase = settings.isNight() ? "Night" : "Day";
                    String timeMessage = String.format("\n\nIt is %02d:00 (%s), Day %d of Month %d, Year %d.",
                            settings.mudHour(), phase, settings.mudDay(), settings.mudMonth(), settings.mudYear());
                    communicationService.sendTextMessage(mobile, timeMessage);
                })
                .then();
    }

    @Override
    public String getDescription() {
        return "Check the current in-game date and time.";
    }

    @Override
    public String getDetailedDescription() {
        return "Syntax: date\n\nDisplays the current time, day, month, and year in the realm.";
    }
}
