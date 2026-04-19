package io.nadia.ai.aimud.prayers;

import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.service.CharacterService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.EffectService;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.SkillService;
import io.nadia.ai.aimud.service.TickService;
import io.nadia.ai.aimud.types.WeatherType;
import io.nadia.ai.aimud.annontation.DivinePrayer;
import org.springframework.stereotype.Component;

@DivinePrayer(name = "rain")
public class RainPrayer extends Prayer {

    private final TickService tickService;

    public RainPrayer(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService, TickService tickService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
        this.tickService = tickService;
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    public String getPrayerName() {
        return "rain";
    }

    @Override
    public Long getPrayerId() {
        return 903L;
    }

    @Override
    public Integer getPrayerLevel() {
        return 15;
    }

    @Override
    public String getDescription() {
        return "Pray for the heavens to open up and nourish the earth with rain.";
    }

    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " prays solemnly for the blessing of rain.");
        communicationService.sendTextMessage(mobile, "\n\nYou pray for rain to wash the earth.");
        tickService.changeWeather(WeatherType.RAIN);
        return true;
    }
}
