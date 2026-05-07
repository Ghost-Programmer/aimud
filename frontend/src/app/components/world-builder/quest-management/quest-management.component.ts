import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Quest, QuestStep, QuestDrop, QuestService } from '../../../services/quest.service';
import { MobileService } from '../../../services/mobile.service';
import { ItemService } from '../../../services/item.service';
import { FactionService } from '../../../services/faction.service';
import { ConfigService } from '../../../services/config.service';
import { SearchableDropdownComponent } from '../../searchable-dropdown/searchable-dropdown.component';

@Component({
  selector: 'app-quest-management',
  standalone: true,
  imports: [CommonModule, FormsModule, SearchableDropdownComponent],
  templateUrl: './quest-management.component.html',
  styleUrls: ['./quest-management.component.css']
})
export class QuestManagementComponent implements OnInit {
  quests: Quest[] = [];
  selectedQuest: Quest | null = null;
  steps: QuestStep[] = [];
  drops: QuestDrop[] = [];
  
  newStep: QuestStep = {
    stepNumber: 1,
    objectiveType: 'TALK_TO_NPC',
    targetCount: 1,
    instructions: ''
  };

  newDrop: QuestDrop = {
    itemId: 0,
    dropChance: 100
  };

  objectiveTypes = ['TALK_TO_NPC', 'GIVE_ITEM', 'KILL_NPC', 'KILL_NPC_TYPE'];

  availableMobiles: any[] = [];
  availableItems: any[] = [];
  availableFactions: any[] = [];
  availableRaces: any[] = [];

  constructor(
    private questService: QuestService,
    private mobileService: MobileService,
    private itemService: ItemService,
    private factionService: FactionService,
    private configService: ConfigService
  ) {}

  itemDisplayFn = (item: any) => `${item.name} (${item.id})`;
  mobileDisplayFn = (mobile: any) => `${mobile.name} (${mobile.id})`;
  factionDisplayFn = (faction: any) => `${faction.name} (${faction.id})`;
  raceDisplayFn = (race: any) => `${race.name} (${race.id})`;

  ngOnInit(): void {
    this.loadQuests();
    this.loadLookupData();
  }

  loadLookupData(): void {
    this.mobileService.getAllMobiles(0, 1000).subscribe(data => this.availableMobiles = data.mobiles);
    this.itemService.getItems(0, 1000, {}).subscribe(data => this.availableItems = data.items);
    this.factionService.getAllFactions(0, 1000).subscribe(data => this.availableFactions = data.factions);
    this.configService.getAllRaces(0, 1000).subscribe(data => this.availableRaces = data.races);
  }

  loadQuests(): void {
    this.questService.getQuests().subscribe(data => {
      this.quests = data;
    });
  }

  deleteQuest(id: number | undefined): void {
    if (id && confirm('Are you sure you want to delete this quest?')) {
      this.questService.deleteQuest(id).subscribe(() => {
        this.loadQuests();
      });
    }
  }

  editQuest(quest: Quest): void {
    this.selectedQuest = { ...quest };
    if (quest.id) {
      this.loadSteps(quest.id);
      this.loadDrops(quest.id);
    }
  }

  createNewQuest(): void {
    this.selectedQuest = { name: '', description: '', level: 1 };
    this.steps = [];
    this.drops = [];
  }

  cancelEdit(): void {
    this.selectedQuest = null;
    this.steps = [];
    this.drops = [];
  }

  loadSteps(questId: number): void {
    this.questService.getQuestSteps(questId).subscribe(data => {
      this.steps = data;
      this.newStep.stepNumber = this.steps.length + 1;
    });
  }

  loadDrops(questId: number): void {
    this.questService.getQuestDrops(questId).subscribe(data => {
      this.drops = data;
    });
  }

  saveQuest(): void {
    if (!this.selectedQuest) return;

    if (this.selectedQuest.id) {
      this.questService.updateQuest(this.selectedQuest.id, this.selectedQuest).subscribe(() => {
        alert('Quest updated successfully');
        this.loadQuests();
        this.selectedQuest = null;
      });
    } else {
      this.questService.createQuest(this.selectedQuest).subscribe(data => {
        alert('Quest created successfully');
        this.loadQuests();
        this.editQuest(data);
      });
    }
  }

  addStep(): void {
    if (!this.selectedQuest || !this.selectedQuest.id) return;
    this.questService.createQuestStep(this.selectedQuest.id, this.newStep).subscribe(() => {
      this.loadSteps(this.selectedQuest!.id!);
      this.newStep = {
        stepNumber: this.steps.length + 2,
        objectiveType: 'TALK_TO_NPC',
        targetCount: 1,
        instructions: ''
      };
    });
  }

  deleteStep(id: number | undefined): void {
    if (id && confirm('Delete this step?')) {
      this.questService.deleteQuestStep(id).subscribe(() => {
        this.loadSteps(this.selectedQuest!.id!);
      });
    }
  }

  addDrop(): void {
    if (!this.selectedQuest || !this.selectedQuest.id) return;
    this.questService.createQuestDrop(this.selectedQuest.id, this.newDrop).subscribe(() => {
      this.loadDrops(this.selectedQuest!.id!);
      this.newDrop = {
        itemId: 0,
        dropChance: 100
      };
    });
  }

  deleteDrop(id: number | undefined): void {
    if (id && confirm('Delete this drop?')) {
      this.questService.deleteQuestDrop(id).subscribe(() => {
        this.loadDrops(this.selectedQuest!.id!);
      });
    }
  }
}
