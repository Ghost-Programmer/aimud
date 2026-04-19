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

@DivinePrayer(name = "sun")
public class SunPrayer extends Prayer {

    private final TickService tickService;

    public SunPrayer(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService, TickService tickService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
        this.tickService = tickService;
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    public String getPrayerName() {
        return "sun";
    }

    @Override
    public Long getPrayerId() {
        return 901L;
    }

    @Override
    public Integer getPrayerLevel() {
        return 5;
    }

    @Override
    public String getDescription() {
        return "Pray for the sky to clear and the sun to shine brightly.";
    }

    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " raises their hands to the sky, praying for the sun to shine.");
        communicationService.sendTextMessage(mobile, "\n\nYou pray for the skies to clear.");
        tickService.changeWeather(WeatherType.SUNNY);
        return true;
    }
}
