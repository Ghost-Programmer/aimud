package io.nadia.ai.aimud.service;

import io.nadia.ai.aimud.model.Item;
import io.nadia.ai.aimud.model.Mobile;
import io.nadia.ai.aimud.model.User;
import io.nadia.ai.aimud.repository.*;
import io.nadia.ai.aimud.repository.*;
import io.nadia.ai.aimud.types.ItemType;
import io.nadia.ai.aimud.types.WearLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CharacterServiceTest {

    @Mock
    private MobileRepository mobileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StatService statService;
    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private CharacterEffectRepository characterEffectRepository;
    @Mock
    private CommunicationService communicationService;
    @Mock
    private RoomService roomService;
    @Mock
    private CharacterClassRepository characterClassRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private ItemService itemService;
    @Mock
    private MobileService mobileService;
    @Mock
    private FactionService factionService;
    @Mock
    private MobileMacroRepository mobileMacroRepository;

    private CharacterService characterService;

    @BeforeEach
    void setUp() {
        characterService = new CharacterService(
                mobileRepository,
                userRepository,
                statService,
                databaseClient,
                characterEffectRepository,
                communicationService,
                roomService,
                characterClassRepository,
                skillRepository,
                itemService,
                mobileService,
                factionService,
                mobileMacroRepository
        );

        lenient().when(characterEffectRepository.deleteByCharacterId(any())).thenReturn(Mono.empty());
        lenient().when(characterEffectRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        configureInventoryDatabaseClientMock();
    }

    private void configureInventoryDatabaseClientMock() {
        DatabaseClient.GenericExecuteSpec executeSpec = mock(DatabaseClient.GenericExecuteSpec.class, RETURNS_DEEP_STUBS);
        lenient().when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        lenient().when(executeSpec.bind(anyString(), any())).thenReturn(executeSpec);
        lenient().when(executeSpec.then()).thenReturn(Mono.empty());
        lenient().when(executeSpec.fetch().rowsUpdated()).thenReturn(Mono.just(1L));
    }

    @Test
    void createCharacter_ShouldInitializeCurrentHpAndManaToMaxValues() {
        Mobile newCharacter = new Mobile();
        newCharacter.setName("FreshHero");
        newCharacter.setStrength(10);
        newCharacter.setDexterity(8);
        newCharacter.setConstitution(12);
        newCharacter.setIntelligence(9);
        newCharacter.setWisdom(7);
        newCharacter.setCharisma(6);

        User user = new User();
        user.setId(42L);
        user.setUsername("jeff");

        Mobile derivedCharacter = new Mobile();
        derivedCharacter.setName("FreshHero");
        derivedCharacter.setStrength(10);
        derivedCharacter.setDexterity(8);
        derivedCharacter.setConstitution(12);
        derivedCharacter.setIntelligence(9);
        derivedCharacter.setWisdom(7);
        derivedCharacter.setCharisma(6);
        derivedCharacter.setUserId(42L);
        derivedCharacter.setMaxHp(330);
        derivedCharacter.setMaxMana(230);

        Mobile savedCharacter = new Mobile();
        savedCharacter.setId(100L);
        savedCharacter.setName("FreshHero");
        savedCharacter.setStrength(10);
        savedCharacter.setDexterity(8);
        savedCharacter.setConstitution(12);
        savedCharacter.setIntelligence(9);
        savedCharacter.setWisdom(7);
        savedCharacter.setCharisma(6);
        savedCharacter.setUserId(42L);
        savedCharacter.setCurrentHp(330);
        savedCharacter.setCurrentMana(230);
        savedCharacter.setMaxHp(330);
        savedCharacter.setMaxMana(230);

        when(userRepository.findByUsername("jeff")).thenReturn(Mono.just(user));
        when(statService.updateCurrentStats(any(Mobile.class)))
                .thenReturn(Mono.just(derivedCharacter))
                .thenReturn(Mono.just(savedCharacter));
        when(mobileRepository.save(any(Mobile.class))).thenAnswer(invocation -> {
            Mobile characterToSave = invocation.getArgument(0);
            Mobile persistedCharacter = new Mobile();
            persistedCharacter.setId(100L);
            persistedCharacter.setName(characterToSave.getName());
            persistedCharacter.setStrength(characterToSave.getStrength());
            persistedCharacter.setDexterity(characterToSave.getDexterity());
            persistedCharacter.setConstitution(characterToSave.getConstitution());
            persistedCharacter.setIntelligence(characterToSave.getIntelligence());
            persistedCharacter.setWisdom(characterToSave.getWisdom());
            persistedCharacter.setCharisma(characterToSave.getCharisma());
            persistedCharacter.setUserId(characterToSave.getUserId());
            persistedCharacter.setCurrentHp(characterToSave.getCurrentHp());
            persistedCharacter.setCurrentMana(characterToSave.getCurrentMana());
            return Mono.just(persistedCharacter);
        });

        StepVerifier.create(characterService.createCharacter("jeff", newCharacter))
                .assertNext(createdCharacter -> {
                    assertThat(createdCharacter.getCurrentHp()).isEqualTo(createdCharacter.getMaxHp());
                    assertThat(createdCharacter.getCurrentMana()).isEqualTo(createdCharacter.getMaxMana());
                    assertThat(createdCharacter.getCurrentHp()).isEqualTo(330);
                    assertThat(createdCharacter.getCurrentMana()).isEqualTo(230);
                })
                .verifyComplete();

        ArgumentCaptor<Mobile> savedCharacterCaptor = ArgumentCaptor.forClass(Mobile.class);
        verify(mobileRepository).save(savedCharacterCaptor.capture());
        Mobile persistedCharacter = savedCharacterCaptor.getValue();
        assertThat(persistedCharacter.getCurrentHp()).isEqualTo(330);
        assertThat(persistedCharacter.getCurrentMana()).isEqualTo(230);
    }

    @Test
    void equipItem_ShouldEquipHeadItemAndReturnOldToInventory() {
        // Arrange
        Mobile character = new Mobile();
        character.setId(1L);
        character.setUserId(99L);
        character.setName("TestHero");

        Item oldHead = new Item();
        oldHead.setId(10L);
        oldHead.setName("Old Helmet");
        character.setHead(oldHead);

        Item newHead = new Item();
        newHead.setId(11L);
        newHead.setName("New Shiny Helmet");
        newHead.setItemType(ItemType.HEAVY_ARMOR);
        newHead.setWearLocation(WearLocation.HEAD);

        List<Item> inventory = new ArrayList<>();
        inventory.add(newHead);
        character.setInventory(inventory);

        when(mobileRepository.save(any(Mobile.class))).thenReturn(Mono.just(character));
        when(mobileRepository.findById(1L)).thenReturn(Mono.just(character));
        when(statService.updateCurrentStats(any(Mobile.class))).thenReturn(Mono.just(character));

        // Act
        StepVerifier.create(characterService.equipItem(character, 11L))
                .assertNext(updatedChar -> {
                    // Assert
                    assertThat(updatedChar.getHead()).isEqualTo(newHead);
                    assertThat(updatedChar.getInventory()).contains(oldHead);
                    assertThat(updatedChar.getInventory()).doesNotContain(newHead);
                })
                .verifyComplete();

        verify(communicationService).sendTextMessage(eq(character), contains("You equip New Shiny Helmet"));
    }

    @Test
    void equipItem_ShouldHandleFingerSlotsCorrectly() {
        // Arrange
        Mobile character = new Mobile();
        character.setId(1L);
        character.setUserId(99L);
        character.setName("TestHero");

        Item ring1 = new Item();
        ring1.setId(21L);
        ring1.setName("Gold Ring");
        ring1.setItemType(ItemType.MISC); // Initially MISC to avoid being equippable if we test that
        ring1.setWearLocation(WearLocation.FINGER);

        // Equippable ring
        Item newRing = new Item();
        newRing.setId(22L);
        newRing.setName("Magic Ring");
        newRing.setItemType(ItemType.LIGHT_ARMOR); // Setting as armor to make it equippable per logic
        newRing.setWearLocation(WearLocation.FINGER);

        character.getInventory().add(newRing);

        when(mobileRepository.save(any(Mobile.class))).thenReturn(Mono.just(character));
        when(mobileRepository.findById(1L)).thenReturn(Mono.just(character));
        when(statService.updateCurrentStats(any(Mobile.class))).thenReturn(Mono.just(character));

        // Test 1: Right finger empty -> goes to Right
        characterService.equipItem(character, 22L).block();
        assertThat(character.getRightFinger()).isEqualTo(newRing);
        assertThat(character.getLeftFinger()).isNull();

        // Test 2: Right full, Left empty -> goes to Left
        Item newRing2 = new Item();
        newRing2.setId(23L);
        newRing2.setName("Power Ring");
        newRing2.setItemType(ItemType.LIGHT_ARMOR);
        newRing2.setWearLocation(WearLocation.FINGER);
        character.getInventory().add(newRing2);

        characterService.equipItem(character, 23L).block();
        assertThat(character.getRightFinger()).isEqualTo(newRing);
        assertThat(character.getLeftFinger()).isEqualTo(newRing2);

        // Test 3: Both full -> replaces Left
        Item newRing3 = new Item();
        newRing3.setId(24L);
        newRing3.setName("Uber Ring");
        newRing3.setItemType(ItemType.LIGHT_ARMOR);
        newRing3.setWearLocation(WearLocation.FINGER);
        character.getInventory().add(newRing3);

        characterService.equipItem(character, 24L).block();
        assertThat(character.getRightFinger()).isEqualTo(newRing);
        assertThat(character.getLeftFinger()).isEqualTo(newRing3);
        assertThat(character.getInventory()).contains(newRing2);
    }

    @Test
    void dropItem_ShouldRemoveFromInventoryAndAddToRoom() {
        // Arrange
        Mobile character = new Mobile();
        character.setId(1L);
        character.setUserId(99L);
        character.setName("TestHero");
        character.setCurrentRoomId(101L);

        Item itemToDrop = new Item();
        itemToDrop.setId(55L);
        itemToDrop.setName("Rusty Sword");

        character.getInventory().add(itemToDrop);

        when(roomService.addItemToRoom(eq(101L), eq(55L))).thenReturn(Mono.empty());
        when(mobileRepository.save(any(Mobile.class))).thenReturn(Mono.just(character));
        when(mobileRepository.findById(1L)).thenReturn(Mono.just(character));
        when(statService.updateCurrentStats(any(Mobile.class))).thenReturn(Mono.just(character));

        // Act
        StepVerifier.create(characterService.dropItem(character, 55L))
                .assertNext(updatedChar -> {
                    // Assert
                    assertThat(updatedChar.getInventory()).isEmpty();
                })
                .verifyComplete();

        verify(roomService).addItemToRoom(101L, 55L);
        verify(communicationService).sendTextMessage(eq(character), contains("You drop Rusty Sword"));
        verify(communicationService).roomMessage(eq(character), contains("TestHero drops Rusty Sword"));
    }
}


