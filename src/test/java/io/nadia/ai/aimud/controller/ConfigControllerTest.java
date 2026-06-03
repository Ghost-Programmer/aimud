package io.nadia.ai.aimud.controller;

import io.nadia.ai.aimud.model.Agent;
import io.nadia.ai.aimud.model.CharacterClass;
import io.nadia.ai.aimud.model.Race;
import io.nadia.ai.aimud.model.SkillRegistry;
import io.nadia.ai.aimud.service.ConfigService;
import io.nadia.ai.aimud.model.dto.ItemTypeDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigControllerTest {

    @Mock
    private ConfigService configService;

    @Test
    void getItemTypesDetails_ReturnsMapOfDetails() {
        ConfigController controller = new ConfigController(configService);
        StepVerifier.create(controller.getItemTypesDetails())
                .assertNext(detailsMap -> {
                    assertThat(detailsMap).containsKey("Weapon");
                    ItemTypeDetails weaponDetails = detailsMap.get("Weapon");
                    assertThat(weaponDetails.getName()).isEqualTo("WEAPON");
                    assertThat(weaponDetails.getLabel()).isEqualTo("Weapon");
                    assertThat(weaponDetails.getProperty1Name()).isNull();
                    assertThat(weaponDetails.getProperty2Name()).isNull();
                    assertThat(weaponDetails.getProperty3Name()).isNull();
                    assertThat(weaponDetails.getProperty4Name()).isNull();

                    assertThat(detailsMap).containsKey("Food");
                    ItemTypeDetails foodDetails = detailsMap.get("Food");
                    assertThat(foodDetails.getName()).isEqualTo("FOOD");
                    assertThat(foodDetails.getLabel()).isEqualTo("Food");
                    assertThat(foodDetails.getProperty1Name()).isEqualTo("Portions / Bites");
                    assertThat(foodDetails.getProperty2Name()).isEqualTo("Hours Satiated");
                    assertThat(foodDetails.getProperty3Name()).isNull();
                    assertThat(foodDetails.getProperty4Name()).isEqualTo("Poisoned Flag");
                })
                .verifyComplete();
    }

    @Test
    void getAllRaces_PlayableOnlyTrueUsesPlayableService() {
        ConfigController controller = new ConfigController(configService);
        Race race = new Race();
        race.setName("Elf");
        when(configService.getPlayableRaces()).thenReturn(Flux.just(race));

        StepVerifier.create(controller.getAllRaces(true, "", 0, 10))
                .assertNext(res -> {
                    @SuppressWarnings("unchecked")
                    java.util.List<Race> list = (java.util.List<Race>) res.get("races");
                    org.assertj.core.api.Assertions.assertThat(list).containsExactly(race);
                })
                .verifyComplete();

        verify(configService).getPlayableRaces();
    }

    @Test
    void updateAgent_NotFoundMapsTo404() {
        ConfigController controller = new ConfigController(configService);
        Agent input = new Agent(null, "title", "content");
        when(configService.updateAgent(9L, input)).thenReturn(Mono.empty());

        StepVerifier.create(controller.updateAgent(9L, input))
                .assertNext(resp -> assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND))
                .verifyComplete();
    }

    @Test
    void getAllSkills_DelegatesToService() {
        ConfigController controller = new ConfigController(configService);
        SkillRegistry skill = SkillRegistry.builder().id(1L).name("Parry").build();
        when(configService.getAllSkills()).thenReturn(Flux.just(skill));

        StepVerifier.create(controller.getAllSkills())
                .expectNext(skill)
                .verifyComplete();

        verify(configService).getAllSkills();
    }

    @Test
    void createCharacterClass_DelegatesToService() {
        ConfigController controller = new ConfigController(configService);
        CharacterClass cc = new CharacterClass();
        cc.setName("Warrior");
        when(configService.createCharacterClass(cc)).thenReturn(Mono.just(cc));

        StepVerifier.create(controller.createCharacterClass(cc))
                .expectNext(cc)
                .verifyComplete();

        verify(configService).createCharacterClass(cc);
    }
}

