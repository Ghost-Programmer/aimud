package io.nadia.ai.aimud.commands;

import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.Skill;
import io.nadia.ai.aimud.service.MobileService;
import io.nadia.ai.aimud.service.CommunicationService;
import io.nadia.ai.aimud.service.SkillService;
import io.nadia.ai.aimud.types.ItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearnCommandTest {

    @Mock
    private CommunicationService communicationService;

    @Mock
    private SkillService skillService;

    @Mock
    private MobileService MobileService;

    private LearnCommand learnCommand;

    @BeforeEach
    void setUp() {
        learnCommand = new LearnCommand(communicationService, skillService, MobileService);
    }

    @Test
    void execute_whenBookMatches_learnsSkillAndConsumesBook() {
        Mobile Mobile = new Mobile();
        Mobile.setId(1L);
        Mobile.setName("Learner");

        Item book = new Item();
        book.setId(10L);
        book.setName("Book of Arcane Basics");
        book.setItemType(ItemType.BOOK);
        book.setProperty1(1000);
        Mobile.setInventory(List.of(book));

        Skill learnedSkill = Skill.builder()
                .name("Spell: Magic Missile")
                .rank(1)
                .characterId(1L)
                .build();

        when(skillService.getSkillNameById(1000L)).thenReturn(Mono.just("Spell: Magic Missile"));
        when(skillService.getSkillRank(Mobile, "Spell: Magic Missile")).thenReturn(0);
        when(skillService.addSkill(Mobile, "Spell: Magic Missile")).thenReturn(Mono.just(learnedSkill));
        when(MobileService.destroyInventoryItem(Mobile, 10L)).thenReturn(Mono.just(Mobile));

        StepVerifier.create(learnCommand.execute(Mobile, "learn arcane"))
                .verifyComplete();

        verify(skillService).addSkill(Mobile, "Spell: Magic Missile");
        verify(MobileService).destroyInventoryItem(Mobile, 10L);
        verify(communicationService).sendTextMessage(eq(Mobile), contains("learn Spell: Magic Missile"));
        verify(communicationService).sendCharacterUpdate(Mobile);
    }

    @Test
    void execute_whenNoMatchingBook_sendsFeedback() {
        Mobile Mobile = new Mobile();
        Mobile.setId(1L);
        Mobile.setName("Learner");
        Mobile.setInventory(List.of());

        StepVerifier.create(learnCommand.execute(Mobile, "learn arcane"))
                .verifyComplete();

        verify(communicationService).sendTextMessage(eq(Mobile), contains("don't have a book"));
        verify(skillService, never()).addSkill(Mobile, "Spell: Magic Missile");
    }

    @Test
    void execute_whenAlreadyKnowsSkill_doesNotConsumeBook() {
        Mobile Mobile = new Mobile();
        Mobile.setId(1L);
        Mobile.setName("Learner");

        Item book = new Item();
        book.setId(10L);
        book.setName("Book of Arcane Basics");
        book.setItemType(ItemType.BOOK);
        book.setProperty1(1000);
        Mobile.setInventory(List.of(book));

        when(skillService.getSkillNameById(1000L)).thenReturn(Mono.just("Spell: Magic Missile"));
        when(skillService.getSkillRank(Mobile, "Spell: Magic Missile")).thenReturn(5);

        StepVerifier.create(learnCommand.execute(Mobile, "learn arcane"))
                .verifyComplete();

        verify(MobileService, never()).destroyInventoryItem(Mobile, 10L);
        verify(communicationService).sendTextMessage(eq(Mobile), contains("already know"));
    }
}


