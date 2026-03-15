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

  // Items selection
  availableItems: Item[] = [];
  selectedItemId: number | null = null;
  currentClassStartingItems: Item[] = [];

  // Skills selection
  availableSkills: any[] = [];
  selectedSkillName: string | null = null;
  currentClassStartingSkills: string[] = [];

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
    this.loadSkills();
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

  // Items
  loadItems() {
    this.itemService.getItems(0, 1000, {}).subscribe(response => {
      this.availableItems = response.items.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
    });
  }

  // Skills
  loadSkills() {
    this.configService.getAllSkills().subscribe(skills => {
      this.availableSkills = skills.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
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
    this.currentClassStartingSkills = [];

    if (cls) {
      this.classForm.patchValue(cls);

      if (cls.startingItems) {
        const itemIds = cls.startingItems.split(',')
          .map((s: string) => parseInt(s.trim()))
          .filter((n: number) => !isNaN(n));

        this.currentClassStartingItems = this.availableItems.filter(i => itemIds.includes(i.id!));
      }

      if (cls.startingSkills) {
        this.currentClassStartingSkills = cls.startingSkills.split(',')
          .map((s: string) => s.trim())
          .filter((s: string) => s.length > 0);
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
    this.currentClassStartingSkills = [];
  }

  // Item List Management
  addSelectedItem() {
    if (this.selectedItemId) {
      const itemId = Number(this.selectedItemId);
      const itemToAdd = this.availableItems.find(i => i.id === itemId);
      if (itemToAdd) {
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

  onSelectItemIdChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.selectedItemId = target.value ? Number(target.value) : null;
  }

  // Skill List Management
  addSelectedSkill() {
    if (this.selectedSkillName) {
      if (!this.currentClassStartingSkills.includes(this.selectedSkillName)) {
        this.currentClassStartingSkills.push(this.selectedSkillName);
        this.updateStartingSkillsFormValue();
      }
      this.selectedSkillName = null;
    }
  }

  removeStartingSkill(index: number) {
    this.currentClassStartingSkills.splice(index, 1);
    this.updateStartingSkillsFormValue();
  }

  updateStartingSkillsFormValue() {
    const names = this.currentClassStartingSkills.join(',');
    this.classForm.patchValue({ startingSkills: names });
  }

  onSelectSkillChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.selectedSkillName = target.value || null;
  }

  // Save/Delete Class
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
}
