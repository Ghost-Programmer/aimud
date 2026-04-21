package io.nadia.ai.aimud.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ModelSmokeTest {

    @Test
    void agent_RecordExposesComponents() {
        Agent agent = new Agent(1L, "Builder", "Prompt content");

        assertThat(agent.id()).isEqualTo(1L);
        assertThat(agent.title()).isEqualTo("Builder");
        assertThat(agent.content()).isEqualTo("Prompt content");
    }

    @Test
    void serverSettings_RecordExposesComponents() {
        LocalDateTime now = LocalDateTime.now();
        ServerSettings settings = new ServerSettings(1L, "AI Mud", true, false, "", 0, 0, 0, 0, now, now, "system", "system");

        assertThat(settings.id()).isEqualTo(1L);
        assertThat(settings.serverName()).isEqualTo("AI Mud");
        assertThat(settings.allowNewUser()).isTrue();
        assertThat(settings.maintenance()).isFalse();
        assertThat(settings.createdBy()).isEqualTo("system");
    }

    @Test
    void characterEffect_ConvenienceConstructorAssignsFields() {
        CharacterEffect characterEffect = new CharacterEffect(10L, 20L, 3);

        assertThat(characterEffect.getCharacterId()).isEqualTo(10L);
        assertThat(characterEffect.getEffectId()).isEqualTo(20L);
        assertThat(characterEffect.getTickCount()).isEqualTo(3);
    }

    @Test
    void race_ConvenienceConstructorAssignsFields() {
        Race race = new Race("Elf", "Graceful", -1, 2, 1, 0, 3, -1);

        assertThat(race.getName()).isEqualTo("Elf");
        assertThat(race.getDescription()).isEqualTo("Graceful");
        assertThat(race.getStrengthMod()).isEqualTo(-1);
        assertThat(race.getIntelligenceMod()).isEqualTo(2);
        assertThat(race.getDexterityMod()).isEqualTo(3);
    }

    @Test
    void user_ConvenienceConstructorAssignsUsernameAndPassword() {
        User user = new User("jeff", "secret");

        assertThat(user.getUsername()).isEqualTo("jeff");
        assertThat(user.getPassword()).isEqualTo("secret");
    }

    @Test
    void faction_SettersAndGettersWork() {
        Faction faction = new Faction();
        faction.setId(1L);
        faction.setName("Guild");
        faction.setDescription("Guild faction");

        assertThat(faction.getId()).isEqualTo(1L);
        assertThat(faction.getName()).isEqualTo("Guild");
        assertThat(faction.getDescription()).isEqualTo("Guild faction");
    }

    @Test
    void store_DefaultsItemsToEmptyList() {
        Store store = new Store();

        assertThat(store.getItems()).isNotNull().isEmpty();
    }

    @Test
    void storeItem_DefaultAvailableIsMinusOne() {
        StoreItem storeItem = new StoreItem();

        assertThat(storeItem.getAvailable()).isEqualTo(-1);
    }

    @Test
    void skill_BuilderCreatesExpectedValues() {
        Skill skill = Skill.builder()
                .id(1L)
                .name("Slash")
                .rank(5)
                .characterId(9L)
                .build();

        assertThat(skill.getId()).isEqualTo(1L);
        assertThat(skill.getName()).isEqualTo("Slash");
        assertThat(skill.getRank()).isEqualTo(5);
        assertThat(skill.getCharacterId()).isEqualTo(9L);
    }

    @Test
    void skillRegistry_BuilderCreatesExpectedValues() {
        SkillRegistry registry = SkillRegistry.builder()
                .id(2L)
                .name("Parry")
                .build();

        assertThat(registry.getId()).isEqualTo(2L);
        assertThat(registry.getName()).isEqualTo("Parry");
    }

    @Test
    void textMessage_AllArgsConstructorAssignsFields() {
        TextMessage message = new TextMessage(7L, "hello");

        assertThat(message.getCharacterId()).isEqualTo(7L);
        assertThat(message.getContent()).isEqualTo("hello");
    }

    @Test
    void targetUpdate_AllArgsConstructorAssignsFields() {
        Mobile character = new Mobile();
        character.setId(1L);
        Mobile target = new Mobile();
        target.setId(2L);

        TargetUpdate update = new TargetUpdate(character, target);

        assertThat(update.getCharacter()).isSameAs(character);
        assertThat(update.getTarget()).isSameAs(target);
    }

    @Test
    void storeDialogEvent_AllArgsConstructorAssignsFields() {
        StoreDialogEvent event = new StoreDialogEvent(11L, 22L, "Bob");

        assertThat(event.getCharacterId()).isEqualTo(11L);
        assertThat(event.getStoreId()).isEqualTo(22L);
        assertThat(event.getShopkeeperName()).isEqualTo("Bob");
    }

    @Test
    void partyUpdate_AllArgsConstructorAssignsFields() {
        PartyUpdate.PartyMemberInfo member = new PartyUpdate.PartyMemberInfo(1L, "Leader", 100, 120, 50, 60);
        PartyUpdate update = new PartyUpdate(9L, 1L, List.of(member));

        assertThat(update.getCharacterId()).isEqualTo(9L);
        assertThat(update.getLeaderId()).isEqualTo(1L);
        assertThat(update.getMembers()).containsExactly(member);
        assertThat(member.getName()).isEqualTo("Leader");
        assertThat(member.getCurrentHp()).isEqualTo(100);
    }

    @Test
    void storePayloadRecords_ExposeComponents() {
        Item item = new Item();
        item.setId(5L);
        item.setName("Sword");

        StorePayloads.StoreItemDTO storeItemDTO = new StorePayloads.StoreItemDTO(5L, item, 3, 120);
        StorePayloads.PlayerItemDTO playerItemDTO = new StorePayloads.PlayerItemDTO(5L, item, 40);
        StorePayloads.StoreDialogPayload payload = new StorePayloads.StoreDialogPayload(
                1L,
                "Merchant",
                60,
                14,
                250,
                List.of(storeItemDTO),
                List.of(playerItemDTO)
        );

        assertThat(storeItemDTO.itemId()).isEqualTo(5L);
        assertThat(storeItemDTO.item()).isSameAs(item);
        assertThat(storeItemDTO.available()).isEqualTo(3);
        assertThat(storeItemDTO.buyPrice()).isEqualTo(120);

        assertThat(playerItemDTO.itemId()).isEqualTo(5L);
        assertThat(playerItemDTO.sellPrice()).isEqualTo(40);

        assertThat(payload.storeId()).isEqualTo(1L);
        assertThat(payload.shopkeeperName()).isEqualTo("Merchant");
        assertThat(payload.storeItems()).containsExactly(storeItemDTO);
        assertThat(payload.playerInventory()).containsExactly(playerItemDTO);
    }
}

