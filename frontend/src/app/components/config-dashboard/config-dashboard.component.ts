import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { ConfigService } from '../../services/config.service';
import { ItemService } from '../../services/item.service';
import { Item } from '../../models/item.model';

@Component({
  selector: 'app-config-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './config-dashboard.component.html',
  styleUrl: './config-dashboard.component.css'
})
export class ConfigDashboardComponent implements OnInit {
  serverSettingsForm: FormGroup;
  races: any[] = [];
  classes: any[] = [];

  selectedRace: any = null;
  raceForm: FormGroup;
  isRaceFormVisible = false;

  selectedClass: any = null;
  classForm: FormGroup;
  isClassFormVisible = false;

  // New logic for starting items selection
  availableItems: Item[] = [];
  selectedItemId: number | null = null;
  currentClassStartingItems: Item[] = [];

  constructor(
    private fb: FormBuilder,
    private configService: ConfigService,
    private itemService: ItemService
  ) {
    this.serverSettingsForm = this.fb.group({
      id: [null],
      serverName: ['', Validators.required],
      allowNewUser: [true],
      maintenance: [false],
      maintenanceText: [''],
      aiSystemPrompt: ['']
    });

    this.raceForm = this.fb.group({
      id: [null],
      name: ['', Validators.required],
      description: ['', Validators.required],
      strengthMod: [0],
      intelligenceMod: [0],
      wisdomMod: [0],
      charismaMod: [0],
      dexterityMod: [0],
      constitutionMod: [0]
    });

    this.classForm = this.fb.group({
      id: [null],
      name: ['', Validators.required],
      description: ['', Validators.required],
      strengthMod: [0],
      intelligenceMod: [0],
      wisdomMod: [0],
      charismaMod: [0],
      dexterityMod: [0],
      constitutionMod: [0],
      startingItems: [''],
      startingSkills: ['']
    });
  }

  ngOnInit() {
    this.loadServerSettings();
    this.loadRaces();
    this.loadClasses();
    this.loadItems();
  }

  // Server Settings
  loadServerSettings() {
    this.configService.getServerSettings().subscribe(settings => {
      this.serverSettingsForm.patchValue(settings);
    });
  }

  saveServerSettings() {
    if (this.serverSettingsForm.valid) {
      this.configService.updateServerSettings(this.serverSettingsForm.value).subscribe(() => {
        alert('Server settings updated');
      });
    }
  }

  // Races
  loadRaces() {
    this.configService.getAllRaces().subscribe(races => {
      this.races = races;
    });
  }

  openRaceForm(race: any = null) {
    this.selectedRace = race;
    if (race) {
      this.raceForm.patchValue(race);
    } else {
      this.raceForm.reset({
        strengthMod: 0, intelligenceMod: 0, wisdomMod: 0,
        charismaMod: 0, dexterityMod: 0, constitutionMod: 0
      });
    }
    this.isRaceFormVisible = true;
  }

  closeRaceForm() {
    this.isRaceFormVisible = false;
    this.selectedRace = null;
  }

  saveRace() {
    if (this.raceForm.valid) {
      const race = this.raceForm.value;
      if (this.selectedRace) {
        this.configService.updateRace(this.selectedRace.id, race).subscribe(() => {
          this.loadRaces();
          this.closeRaceForm();
        });
      } else {
        this.configService.createRace(race).subscribe(() => {
          this.loadRaces();
          this.closeRaceForm();
        });
      }
    }
  }

  deleteRace(id: number) {
    if (confirm('Are you sure you want to delete this race?')) {
      this.configService.deleteRace(id).subscribe(() => {
        this.loadRaces();
      });
    }
  }

  // Items for Class dropdown
  loadItems() {
    // We assume 1000 items is enough to load for the dropdown,
    // like the implementation in effect-dialog
    this.itemService.getItems(0, 1000, {}).subscribe(response => {
      this.availableItems = response.items.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
    });
  }

  // Classes
  loadClasses() {
    this.configService.getAllCharacterClasses().subscribe(classes => {
      this.classes = classes;
    });
  }

  openClassForm(cls: any = null) {
    this.selectedClass = cls;
    this.currentClassStartingItems = [];
    if (cls) {
      this.classForm.patchValue(cls);
      if (cls.startingItems) {
        const itemIds = cls.startingItems.split(',')
          .map((s: string) => parseInt(s.trim()))
          .filter((n: number) => !isNaN(n));

        this.currentClassStartingItems = this.availableItems.filter(i => itemIds.includes(i.id!));
      }
    } else {
      this.classForm.reset({
        strengthMod: 0, intelligenceMod: 0, wisdomMod: 0,
        charismaMod: 0, dexterityMod: 0, constitutionMod: 0,
        startingItems: '', startingSkills: ''
      });
    }
    this.isClassFormVisible = true;
  }

  closeClassForm() {
    this.isClassFormVisible = false;
    this.selectedClass = null;
    this.currentClassStartingItems = [];
  }

  addSelectedItem() {
    if (this.selectedItemId) {
      // parse it just in case it is bound as a string from the select option
      const itemId = Number(this.selectedItemId);
      const itemToAdd = this.availableItems.find(i => i.id === itemId);
      if (itemToAdd) {
        // Allow duplicates, but typical use case might be unique starting items
        this.currentClassStartingItems.push(itemToAdd);
        this.updateStartingItemsFormValue();
      }
      this.selectedItemId = null;
    }
  }

  removeStartingItem(index: number) {
    this.currentClassStartingItems.splice(index, 1);
    this.updateStartingItemsFormValue();
  }

  updateStartingItemsFormValue() {
    const ids = this.currentClassStartingItems.map(i => i.id).join(',');
    this.classForm.patchValue({ startingItems: ids });
  }

  saveClass() {
    if (this.classForm.valid) {
      const cls = this.classForm.value;
      if (this.selectedClass) {
        this.configService.updateCharacterClass(this.selectedClass.id, cls).subscribe(() => {
          this.loadClasses();
          this.closeClassForm();
        });
      } else {
        this.configService.createCharacterClass(cls).subscribe(() => {
          this.loadClasses();
          this.closeClassForm();
        });
      }
    }
  }

  deleteClass(id: number) {
    if (confirm('Are you sure you want to delete this class?')) {
      this.configService.deleteCharacterClass(id).subscribe(() => {
        this.loadClasses();
      });
    }
  }

  onSelectItemIdChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.selectedItemId = target.value ? Number(target.value) : null;
  }
}
