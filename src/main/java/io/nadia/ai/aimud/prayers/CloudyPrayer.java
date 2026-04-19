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

@DivinePrayer(name = "cloudy")
public class CloudyPrayer extends Prayer {

    private final TickService tickService;

    public CloudyPrayer(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService, TickService tickService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
        this.tickService = tickService;
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    public String getPrayerName() {
        return "cloudy";
    }

    @Override
    public Long getPrayerId() {
        return 902L;
    }

    @Override
    public Integer getPrayerLevel() {
        return 10;
    }

    @Override
    public String getDescription() {
        return "Pray for clouds to gather and shield the land from the harsh sun.";
    }

    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " chants softly, calling upon the clouds to gather.");
        communicationService.sendTextMessage(mobile, "\n\nYou pray for the skies to become cloudy.");
        tickService.changeWeather(WeatherType.CLOUDY);
        return true;
    }
}
