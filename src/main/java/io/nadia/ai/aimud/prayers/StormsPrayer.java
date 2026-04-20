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
/**
 * StormsPrayer standard implementation layer.
 * Pray for torrential storms to ravage the lands.
 */

@DivinePrayer(name = "storms")
public class StormsPrayer extends Prayer {

    private final TickService tickService;

    public StormsPrayer(SkillService skillService, MobileService mobileService, CharacterService characterService, CommunicationService communicationService, EffectService effectService, TickService tickService) {
        super(skillService, mobileService, characterService, communicationService, effectService);
        this.tickService = tickService;
    }

    @Override
    public boolean requiresTarget() {
        return false;
    }

    @Override
    public String getPrayerName() {
        return "storms";
    }

    @Override
    public Long getPrayerId() {
        return 904L;
    }

    @Override
    public Integer getPrayerLevel() {
        return 20;
    }

    @Override
    public String getDescription() {
        return "Pray for torrential storms to ravage the lands.";
    }

    @Override
    public boolean pray(Mobile mobile, Prayer prayer, Mobile target) {
        communicationService.roomMessage(mobile, "\n\n" + mobile.getName() + " channels their energy, beseeching the heavens for a violent storm.");
        communicationService.sendTextMessage(mobile, "\n\nYou pray for the winds to howl and the storm to rage.");
        tickService.changeWeather(WeatherType.STORMS);
        return true;
    }
}
