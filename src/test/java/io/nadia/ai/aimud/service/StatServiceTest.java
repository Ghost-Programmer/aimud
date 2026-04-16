package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Mobile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StatServiceTest {

    // StatService needs repo mocks for the reactive path, but updateMobileStats is pure-sync
    @Mock private io.nadia.ai.aimud.repository.RaceRepository raceRepository;
    @Mock private io.nadia.ai.aimud.repository.CharacterClassRepository characterClassRepository;
    @Mock private RoomService roomService;
    @Mock private io.nadia.ai.aimud.repository.ItemRepository itemRepository;
    @Mock private io.nadia.ai.aimud.repository.EffectRepository effectRepository;
    @Mock private io.nadia.ai.aimud.repository.CharacterEffectRepository characterEffectRepository;
    @Mock private ItemService itemService;
    @Mock private io.nadia.ai.aimud.repository.SkillRepository skillRepository;

    private StatService statService;

    @BeforeEach
    void setUp() {
        statService = new StatService(raceRepository, characterClassRepository, roomService,
                itemRepository, effectRepository, characterEffectRepository, itemService, skillRepository);
    }

    private Mobile mobileWithStats(int str, int dex, int con, int intel, int wis, int cha) {
        Mobile m = new Mobile();
        m.setStrength(str);
        m.setDexterity(dex);
        m.setConstitution(con);
        m.setIntelligence(intel);
        m.setWisdom(wis);
        m.setCharisma(cha);
        return m;
    }

    @Test
    void updateMobileStats_MaxHpUsesConAndStr() {
        Mobile m = mobileWithStats(10, 8, 12, 9, 7, 6);
        statService.updateMobileStats(m);
        // maxHp = 100 + (con * 15) + (str * 5) = 100 + 180 + 50 = 330
        assertThat(m.getMaxHp()).isEqualTo(330);
    }

    @Test
    void updateMobileStats_MaxManaUsesIntelligence() {
        Mobile m = mobileWithStats(10, 8, 12, 9, 7, 6);
        statService.updateMobileStats(m);
        // maxMana = 50 + (intel * 20) = 50 + 180 = 230
        assertThat(m.getMaxMana()).isEqualTo(230);
    }

    @Test
    void updateMobileStats_CurrentHpInitialisesToMaxIfZero() {
        Mobile m = mobileWithStats(10, 8, 12, 9, 7, 6);
        m.setCurrentHp(0);
        statService.updateMobileStats(m);
        assertThat(m.getCurrentHp()).isEqualTo(m.getMaxHp());
    }

    @Test
    void updateMobileStats_CurrentHpClampsToMaxIfOver() {
        Mobile m = mobileWithStats(10, 8, 12, 9, 7, 6);
        statService.updateMobileStats(m);
        int computedMax = m.getMaxHp();
        m.setCurrentHp(computedMax + 500);
        statService.updateMobileStats(m);
        assertThat(m.getCurrentHp()).isEqualTo(computedMax);
    }

    @Test
    void updateMobileStats_PhysicalAttackUsesStrAndDex() {
        Mobile m = mobileWithStats(10, 8, 12, 9, 7, 6);
        statService.updateMobileStats(m);
        // physicalAttack = (str * 2) + (dex * 0.5) = 20 + 4 = 24
        assertThat(m.getPhysicalAttack()).isEqualTo(24.0);
    }

    @Test
    void updateMobileStats_MagicAttackUsesIntelAndWis() {
        Mobile m = mobileWithStats(10, 8, 12, 9, 7, 6);
        statService.updateMobileStats(m);
        // magicAttack = (intel * 2.5) + (wis * 0.5) = 22.5 + 3.5 = 26.0
        assertThat(m.getMagicAttack()).isEqualTo(26.0);
    }

    @Test
    void updateMobileStats_ArmorUsesStrAndCon() {
        Mobile m = mobileWithStats(10, 8, 12, 9, 7, 6);
        statService.updateMobileStats(m);
        // armor = str + (con * 1.5) = 10 + 18 = 28
        assertThat(m.getArmor()).isEqualTo(28.0);
    }

    @Test
    void updateMobileStats_HpRegenIsAtLeastOne() {
        Mobile m = mobileWithStats(1, 1, 1, 1, 1, 1);
        statService.updateMobileStats(m);
        assertThat(m.getHpRegen()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void updateMobileStats_ManaRegenIsAtLeastOne() {
        Mobile m = mobileWithStats(1, 1, 1, 1, 1, 1);
        statService.updateMobileStats(m);
        assertThat(m.getManaRegen()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void updateMobileStats_DodgeChanceIncreasesWithDex() {
        Mobile lowDex = mobileWithStats(10, 10, 10, 10, 10, 10);
        Mobile highDex = mobileWithStats(10, 100, 10, 10, 10, 10);
        statService.updateMobileStats(lowDex);
        statService.updateMobileStats(highDex);
        assertThat(highDex.getDodgeChance()).isGreaterThan(lowDex.getDodgeChance());
    }

    @Test
    void updateMobileStats_ChallengeRatingIsPositive() {
        Mobile m = mobileWithStats(10, 8, 12, 9, 7, 6);
        statService.updateMobileStats(m);
        assertThat(m.getChallengeRating()).isGreaterThan(0f);
    }

    @Test
    void updateMobileStats_HigherStatsYieldHigherChallengeRating() {
        Mobile weak = mobileWithStats(5, 5, 5, 5, 5, 5);
        Mobile strong = mobileWithStats(20, 20, 20, 20, 20, 20);
        statService.updateMobileStats(weak);
        statService.updateMobileStats(strong);
        assertThat(strong.getChallengeRating()).isGreaterThan(weak.getChallengeRating());
    }

    @Test
    void updateMobileStats_CurrentStatsMatchBaseWhenNoEquipment() {
        Mobile m = mobileWithStats(12, 14, 10, 8, 9, 11);
        statService.updateMobileStats(m);
        assertThat(m.getCurrentStrength()).isEqualTo(12);
        assertThat(m.getCurrentDexterity()).isEqualTo(14);
        assertThat(m.getCurrentConstitution()).isEqualTo(10);
        assertThat(m.getCurrentIntelligence()).isEqualTo(8);
        assertThat(m.getCurrentWisdom()).isEqualTo(9);
        assertThat(m.getCurrentCharisma()).isEqualTo(11);
    }
}

