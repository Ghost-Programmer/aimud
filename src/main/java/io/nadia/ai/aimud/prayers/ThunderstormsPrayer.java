package io.nadia.ai.aimud.prayers;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.EffectService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.SkillService;
import io.nadia.ai.aimud.service.TickService;
import io.nadia.ai.aimud.types.WeatherType;
import io.nadia.ai.aimud.annontation.DivinePrayer;
import org.springframework.stereotype.Component;
/**
 * ThunderstormsPrayer standard implementation layer.
 * Pray for the wrath of thunder and lightning to strike the earth.
 */

@DivinePrayer(name = "thunderstorms")
public class ThunderstormsPrayer extends Prayer {

    private final TickService tickService;

    public ThunderstormsPrayer(SkillService skillService, MobileService mobileService, CommunicationService communicationService, EffectService effectService, TickService tickService) {
        super(skillService, mobileService, communicationService, effectService);
        this.tickService = tickService;
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    public String getPrayerName() {
        return "thunderstorms";
    }

    @Override
    public Long getPrayerId() {
        return 905L;
    }

    @Override
    public Integer getPrayerLevel() {
        return 25;
    }

    @Override
    public String getDescription() {
        return "Pray for the wrath of thunder and lightning to strike the earth.";
    }

    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " raises their arms, evoking the terrifying power of thunder and lightning.");
        communicationService.sendTextMessage(mobile, "\n\nYou pray for a violent thunderstorm to descend.");
        tickService.changeWeather(WeatherType.THUNDERSTORMS);
        return true;
    }
}

