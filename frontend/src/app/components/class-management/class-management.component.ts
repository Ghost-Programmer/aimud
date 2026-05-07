import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfigService } from '../../services/config.service';
import { ItemService } from '../../services/item.service';
import { Item } from '../../models/item.model';

@Component({
  selector: 'app-class-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './class-management.component.html',
  styleUrl: './class-management.component.css'
})
export class ClassManagementComponent implements OnInit {
  classes: any[] = [];
  totalItems: number = 0;
  page: number = 0;
  size: number = 10;
  search: string = '';
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
    this.classForm = this.fb.group({
      id: [null],
      name: ['', Validators.required],
      description: ['', Validators.required],
      npcOnly: [false],
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
    this.loadClasses();
    this.loadItems();
    this.loadSkills();
  }

  Math = Math;

  loadClasses() {
    this.configService.getAllCharacterClasses(this.page, this.size, this.search).subscribe(res => {
      this.classes = res.classes;
      this.totalItems = res.total;
    });
  }

  onSearch() {
    this.page = 0;
    this.loadClasses();
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.loadClasses();
    }
  }

  nextPage() {
    if ((this.page + 1) * this.size < this.totalItems) {
      this.page++;
      this.loadClasses();
    }
  }

  loadItems() {
    this.itemService.getItems(0, 1000, {}).subscribe(response => {
      this.availableItems = response.items.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
    });
  }

  loadSkills() {
    this.configService.getAllSkills().subscribe(skills => {
      this.availableSkills = skills.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
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
        id: null,
        name: '',
        description: '',
        npcOnly: false,
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
    this.classForm.patchValue({startingItems: ids});
  }

  onSelectItemIdChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.selectedItemId = target.value ? Number(target.value) : null;
  }

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
    this.classForm.patchValue({startingSkills: names});
  }

  onSelectSkillChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.selectedSkillName = target.value || null;
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

  deleteClass(id: number, event?: Event) {
    event?.stopPropagation();

    if (confirm('Are you sure you want to delete this class?')) {
      this.configService.deleteCharacterClass(id).subscribe(() => {
        this.loadClasses();
      });
    }
  }
}
