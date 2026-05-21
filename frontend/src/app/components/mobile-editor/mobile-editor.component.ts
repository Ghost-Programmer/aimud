import {Component, OnInit, OnDestroy} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MobileService} from '../../services/mobile.service';
import {Mobile} from '../../models/mobile.model';
import {ItemService} from '../../services/item.service';
import {ConfigService} from '../../services/config.service';
import {FactionService} from '../../services/faction.service';
import {Faction} from '../../models/faction.model';
import {StoreService, Store} from '../../services/store.service';
import {SearchableDropdownComponent} from '../searchable-dropdown/searchable-dropdown.component';

import {Subscription} from 'rxjs';

@Component({
  selector: 'app-mobile-editor',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, SearchableDropdownComponent],
  templateUrl: './mobile-editor.component.html',
  styleUrls: ['./mobile-editor.component.css']
})
export class MobileEditorComponent implements OnInit, OnDestroy {
  mobiles: Mobile[] = [];
  totalMobiles: number = 0;
  page: number = 0;
  size: number = 10;
  selectedMobile: Mobile | null = null;
  mobileForm!: FormGroup;

  availableItems: any[] = [];
  availableSkills: any[] = [];
  availableRaces: any[] = [];
  availableClasses: any[] = [];
  stores: Store[] = [];
  factions: Faction[] = [];
  factionRatings: { [key: number]: number } = {};
  availableAgents: any[] = [];

  // Adjusted to match backend JSON mapping of WearLocation
  wearLocations = ['Head', 'Chest', 'Legs', 'Feet', 'Arms', 'Hands', 'Finger', 'Wrist', 'Neck', 'Ear', 'Face', 'Waist', 'Primary', 'Offhand'];

  // To display dropdown values correctly
  availableItemsByLocation: { [key: string]: any[] } = {};

  // Search models for the UI
  equipSearchTexts: { [key: string]: string } = {};
  invSearchTexts: string[] = [];

  private storeSub!: Subscription;

  constructor(
    private fb: FormBuilder,
    private mobileService: MobileService,
    private itemService: ItemService,
    private configService: ConfigService,
    private factionService: FactionService,
    private storeService: StoreService
  ) {
    this.createForm();
  }

  get inventoryControls() {
    return (this.mobileForm.get('inventory') as FormArray);
  }

  get skillsControls() {
    return (this.mobileForm.get('skills') as FormArray);
  }

  ngOnInit(): void {
    this.loadMobiles();
    this.loadItems();
    this.loadSkills();
    this.loadRacesAndClasses();
    this.factionService.getAllFactions(0, 1000).subscribe(res => this.factions = res.factions);
    this.loadStores();
    this.configService.getAllAgents(0, 1000).subscribe(res => this.availableAgents = res.agents);
    this.storeSub = this.storeService.storesUpdated$.subscribe(() => {
      this.loadStores();
    });
  }

  ngOnDestroy(): void {
    if (this.storeSub) {
      this.storeSub.unsubscribe();
    }
  }

  loadStores() {
    this.storeService.getAllStores(0, 1000).subscribe({
      next: (res) => this.stores = [...res.stores].sort((a, b) => a.name.localeCompare(b.name)),
      error: (err) => console.error('Error loading stores', err)
    });
  }

  loadRacesAndClasses() {
    this.configService.getAllRaces(0, 1000).subscribe({
      next: (res) => this.availableRaces = [...res.races].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? '')),
      error: (err) => console.error('Error loading races', err)
    });
    this.configService.getAllCharacterClasses(0, 1000).subscribe({
      next: (res) => this.availableClasses = [...res.classes].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? '')),
      error: (err) => console.error('Error loading classes', err)
    });
  }

  createForm() {
    this.mobileForm = this.fb.group({
      id: [null],
      name: ['', Validators.required],
      description: [''],
      strength: [10],
      dexterity: [10],
      constitution: [10],
      intelligence: [10],
      wisdom: [10],
      charisma: [10],
      currentHp: [100],
      currentMana: [50],
      currentRoomId: [1],
      factionId: [null],
      storeId: [null],
      raceId: [null],
      classId: [null],

      hateHealer: [false],
      hateDebuffer: [false],
      hateWizard: [false],
      hateCleric: [false],
      hateSinger: [false],
      willFollow: [false],
      willLoot: [false],
      usesAi: [false],
      agent: [null],
      nonCombat: [false],
      isWorldLog: [false],

      actions: this.fb.array([]),

      // Equipment
      headId: [null],
      chestId: [null],
      legsId: [null],
      feetId: [null],
      armsId: [null],
      handsId: [null],
      rightFingerId: [null],
      leftFingerId: [null],
      rightWristId: [null],
      leftWristId: [null],
      neckId: [null],
      leftEarId: [null],
      rightEarId: [null],
      faceId: [null],
      waistId: [null],
      primaryId: [null],
      offhandId: [null],

      // Inventory
      inventory: this.fb.array([]),

      // Skills
      skills: this.fb.array([])
    });
  }

  get actionsControls() {
    return this.mobileForm.get('actions') as FormArray;
  }

  addAction() {
    this.actionsControls.push(this.fb.group({
      description: ['', Validators.required],
      actionCommand: ['', Validators.required]
    }));
  }

  removeAction(index: number) {
    this.actionsControls.removeAt(index);
  }

  loadMobiles() {
    this.mobileService.getAllMobiles(this.page, this.size).subscribe({
      next: (data) => {
        this.mobiles = data.mobiles;
        this.totalMobiles = data.total;
      },
      error: (err) => console.error('Error loading mobiles', err)
    });
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.loadMobiles();
    }
  }

  nextPage() {
    if ((this.page + 1) * this.size < this.totalMobiles) {
      this.page++;
      this.loadMobiles();
    }
  }

  loadItems() {
    // Fetch a large enough page
    this.itemService.getItems(0, 1000, {}).subscribe({
      next: (data) => {
        this.availableItems = [...data.items].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''));

        // Group by wear location for the dropdowns
        this.wearLocations.forEach(loc => {
          this.availableItemsByLocation[loc] = this.availableItems.filter(item =>
            item.wearLocation === loc ||
            // Fallbacks in case the backend sends something else, or for generic slot mapping
            (loc.includes('Finger') && item.wearLocation === 'Finger') ||
            (loc.includes('Wrist') && item.wearLocation === 'Wrist') ||
            (loc.includes('Ear') && item.wearLocation === 'Ear')
          );
        });
      },
      error: (err) => console.error('Error loading items', err)
    });
  }

  loadSkills() {
    this.configService.getAllSkills().subscribe({
      next: (data) => this.availableSkills = [...data].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? '')),
      error: (err) => console.error('Error loading skills', err)
    });
  }

  getEquipItems(loc: string) {
    return this.availableItemsByLocation[loc]?.length ? this.availableItemsByLocation[loc] : this.availableItems;
  }

  itemDisplayFn = (item: any) => `${item.name} (${item.id})`;
  raceDisplayFn = (race: any) => `${race.name} (${race.id})`;
  classDisplayFn = (c: any) => `${c.name} (${c.id})`;

  getFilteredInvItems(index: number) {
    const search = this.invSearchTexts[index]?.toLowerCase();
    const currentId = this.inventoryControls.at(index).get('id')?.value;

    if (!search) {
      return this.availableItems;
    }

    return this.availableItems.filter(item =>
      item.name.toLowerCase().includes(search) || item.id === currentId
    );
  }

  addInventoryItem() {
    this.inventoryControls.push(this.fb.group({
      id: [null, Validators.required]
    }));
    this.invSearchTexts.push('');
  }

  removeInventoryItem(index: number) {
    this.inventoryControls.removeAt(index);
    this.invSearchTexts.splice(index, 1);
  }

  addSkill() {
    this.skillsControls.push(this.fb.group({
      name: ['', Validators.required],
      rank: [1, [Validators.required, Validators.min(1)]]
    }));
  }

  removeSkill(index: number) {
    this.skillsControls.removeAt(index);
  }

  selectMobile(mobile: Mobile) {
    this.selectedMobile = mobile;

    // Clear arrays and search states
    this.invSearchTexts = [];
    while (this.inventoryControls.length) {
      this.inventoryControls.removeAt(0);
    }
    while (this.skillsControls.length) {
      this.skillsControls.removeAt(0);
    }
    this.equipSearchTexts = {};

    if (mobile.inventory) {
      mobile.inventory.forEach(item => {
        this.inventoryControls.push(this.fb.group({
          id: [item.id]
        }));
        this.invSearchTexts.push('');
      });
    }

    if (mobile.id) {
      this.factionService.getMobileFactionRatings(mobile.id).subscribe(ratings => {
        this.factionRatings = ratings || {};
      });
    } else {
      this.factionRatings = {};
    }

    if (mobile.skills) {
      mobile.skills.forEach((skill: any) => {
        this.skillsControls.push(this.fb.group({
          name: [skill.name],
          rank: [skill.rank]
        }));
      });
    }

    // Load actions
    while (this.actionsControls.length) {
      this.actionsControls.removeAt(0);
    }
    if (mobile.id) {
      this.mobileService.getMobileActions(mobile.id).subscribe(actions => {
        actions.forEach(a => {
          this.actionsControls.push(this.fb.group({
            description: [a.description],
            actionCommand: [a.actionCommand]
          }));
        });
      });
    }

    this.mobileForm.patchValue(mobile);
  }

  createNew() {
    this.selectedMobile = null;
    this.mobileForm.reset({
      description: '',
      strength: 10,
      dexterity: 10,
      constitution: 10,
      intelligence: 10,
      wisdom: 10,
      charisma: 10,
      currentHp: 100,
      currentMana: 50,
      currentRoomId: 1,
      raceId: null,
      classId: null,
      hateHealer: false,
      hateDebuffer: false,
      hateWizard: false,
      hateCleric: false,
      hateSinger: false,
      willFollow: false,
      willLoot: false,
      usesAi: false,
      agent: null,
      nonCombat: false,
      factionId: null,
      storeId: null
    });
    this.invSearchTexts = [];
    this.equipSearchTexts = {};
    this.factionRatings = {};
    while (this.inventoryControls.length) this.inventoryControls.removeAt(0);
    while (this.skillsControls.length) this.skillsControls.removeAt(0);
    while (this.actionsControls.length) this.actionsControls.removeAt(0);
  }

  save() {
    if (this.mobileForm.invalid) return;

    const formValue = this.mobileForm.value;

    formValue.inventory = formValue.inventory.map((i: any) => {
      return this.availableItems.find(item => item.id == i.id) || {id: i.id};
    });

    if (formValue.id) {
      this.mobileService.updateMobile(formValue.id, formValue).subscribe({
        next: (updated) => {
          this.mobileService.saveMobileActions(formValue.id, formValue.actions || []).subscribe(() => {
            this.factionService.updateMobileFactionRatings(formValue.id, this.factionRatings).subscribe({
              next: () => {
                this.loadMobiles();
                this.selectedMobile = null;
              }
            });
          });
        }
      });
    } else {
      this.mobileService.createMobile(formValue).subscribe({
        next: (created: any) => {
          if (created && created.id) {
              this.mobileService.saveMobileActions(created.id, formValue.actions || []).subscribe(() => {
                this.factionService.updateMobileFactionRatings(created.id, this.factionRatings).subscribe({
                  next: () => {
                    this.loadMobiles();
                    this.selectedMobile = null;
                  }
                });
              });
          } else {
            this.loadMobiles();
            this.selectedMobile = null;
          }
        }
      });
    }
  }

  deleteMobile(mobile: Mobile) {
    if (confirm(`Are you sure you want to delete ${mobile.name}?`)) {
      this.mobileService.deleteMobile(mobile.id!).subscribe({
        next: () => this.loadMobiles()
      });
    }
  }
}
