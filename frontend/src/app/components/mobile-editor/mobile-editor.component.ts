import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { MobileService } from '../../services/mobile.service';
import { Mobile } from '../../models/mobile.model';
import { ItemService } from '../../services/item.service';
import { ConfigService } from '../../services/config.service';

@Component({
  selector: 'app-mobile-editor',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './mobile-editor.component.html',
  styleUrls: ['./mobile-editor.component.css']
})
export class MobileEditorComponent implements OnInit {
  mobiles: Mobile[] = [];
  selectedMobile: Mobile | null = null;
  mobileForm!: FormGroup;

  availableItems: any[] = [];
  availableSkills: any[] = [];

  // Adjusted to match backend JSON mapping of WearLocation
  wearLocations = ['Head', 'Chest', 'Legs', 'Feet', 'Arms', 'Hands', 'Finger', 'Wrist', 'Neck', 'Ear', 'Face', 'Waist', 'Primary', 'Offhand'];

  // To display dropdown values correctly
  availableItemsByLocation: { [key: string]: any[] } = {};

  // Search models for the UI
  equipSearchTexts: { [key: string]: string } = {};
  invSearchTexts: string[] = [];

  constructor(
    private fb: FormBuilder,
    private mobileService: MobileService,
    private itemService: ItemService,
    private configService: ConfigService
  ) {
    this.createForm();
  }

  ngOnInit(): void {
    this.loadMobiles();
    this.loadItems();
    this.loadSkills();
  }

  createForm() {
    this.mobileForm = this.fb.group({
      id: [null],
      name: ['', Validators.required],
      strength: [10],
      dexterity: [10],
      constitution: [10],
      intelligence: [10],
      wisdom: [10],
      charisma: [10],
      currentHp: [100],
      currentMana: [50],
      currentRoomId: [1],

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

  loadMobiles() {
    this.mobileService.getAllMobiles().subscribe({
      next: (data) => this.mobiles = [...data].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? '')),
      error: (err) => console.error('Error loading mobiles', err)
    });
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

  get inventoryControls() {
    return (this.mobileForm.get('inventory') as FormArray);
  }

  get skillsControls() {
    return (this.mobileForm.get('skills') as FormArray);
  }

  getFilteredEquipItems(loc: string) {
    const items = this.availableItemsByLocation[loc]?.length ? this.availableItemsByLocation[loc] : this.availableItems;
    const search = this.equipSearchTexts[loc]?.toLowerCase();

    // Always make sure the currently selected item is in the list, even if search filters it out
    const controlName = loc.toLowerCase().replace('_', '') + 'Id';
    const currentId = this.mobileForm.get(controlName)?.value;

    if (!search) {
        return items;
    }

    return items.filter(item =>
        item.name.toLowerCase().includes(search) || item.id === currentId
    );
  }

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

    if (mobile.skills) {
      mobile.skills.forEach((skill: any) => {
        this.skillsControls.push(this.fb.group({
          name: [skill.name],
          rank: [skill.rank]
        }));
      });
    }

    this.mobileForm.patchValue(mobile);
  }

  createNew() {
    this.selectedMobile = null;
    this.mobileForm.reset({
      strength: 10,
      dexterity: 10,
      constitution: 10,
      intelligence: 10,
      wisdom: 10,
      charisma: 10,
      currentHp: 100,
      currentMana: 50,
      currentRoomId: 1
    });
    this.invSearchTexts = [];
    this.equipSearchTexts = {};
    while (this.inventoryControls.length) this.inventoryControls.removeAt(0);
    while (this.skillsControls.length) this.skillsControls.removeAt(0);
  }

  save() {
    if (this.mobileForm.invalid) return;

    const formValue = this.mobileForm.value;

    // Transform inventory from [{id: 1}] to Item objects
    formValue.inventory = formValue.inventory.map((i: any) => {
      return this.availableItems.find(item => item.id == i.id) || { id: i.id };
    });

    if (formValue.id) {
      this.mobileService.updateMobile(formValue.id, formValue).subscribe({
        next: () => {
          this.loadMobiles();
          this.selectedMobile = null;
        }
      });
    } else {
      this.mobileService.createMobile(formValue).subscribe({
        next: () => {
          this.loadMobiles();
          this.selectedMobile = null;
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
