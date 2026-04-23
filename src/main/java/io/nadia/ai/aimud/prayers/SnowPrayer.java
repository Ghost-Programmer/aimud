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
 * SnowPrayer standard implementation layer.
 * Pray for a heavy blanket of snow to cover the landscape.
 */

@DivinePrayer(name = "snow")
public class SnowPrayer extends Prayer {

    private final TickService tickService;

    public SnowPrayer(SkillService skillService, MobileService mobileService, CommunicationService communicationService, EffectService effectService, TickService tickService) {
        super(skillService, mobileService, communicationService, effectService);
        this.tickService = tickService;
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    public String getPrayerName() {
        return "snow";
    }

    @Override
    public Long getPrayerId() {
        return 906L;
    }

    @Override
    public Integer getPrayerLevel() {
        return 30;
    }

    @Override
    public String getDescription() {
        return "Pray for a heavy blanket of snow to cover the landscape.";
    }

    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " prays quietly, asking for the gentle descent of freezing snow.");
        communicationService.sendTextMessage(mobile, "\n\nYou pray for the skies to bring forth snow.");
        tickService.changeWeather(WeatherType.SNOW);
        return true;
    }
}

