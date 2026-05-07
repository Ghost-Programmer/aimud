import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FactionService } from '../../services/faction.service';

@Component({
  selector: 'app-faction-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './faction-management.component.html',
  styleUrl: './faction-management.component.css'
})
export class FactionManagementComponent implements OnInit {
  factions: any[] = [];
  totalItems: number = 0;
  page: number = 0;
  size: number = 10;
  selectedFaction: any = null;
  factionForm: FormGroup;
  isFactionFormVisible = false;

  constructor(
    private fb: FormBuilder,
    private factionService: FactionService
  ) {
    this.factionForm = this.fb.group({
      id: [null],
      name: ['', Validators.required],
      description: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.loadFactions();
  }

  Math = Math;

  loadFactions() {
    this.factionService.getAllFactions(this.page, this.size).subscribe(res => {
      this.factions = res.factions;
      this.totalItems = res.total;
    });
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.loadFactions();
    }
  }

  nextPage() {
    if ((this.page + 1) * this.size < this.totalItems) {
      this.page++;
      this.loadFactions();
    }
  }

  openFactionForm(faction: any = null) {
    this.selectedFaction = faction;
    if (faction) {
      this.factionForm.patchValue(faction);
    } else {
      this.factionForm.reset({
        id: null,
        name: '',
        description: ''
      });
    }
    this.isFactionFormVisible = true;
  }

  closeFactionForm() {
    this.isFactionFormVisible = false;
    this.selectedFaction = null;
  }

  saveFaction() {
    if (this.factionForm.valid) {
      const faction = this.factionForm.value;
      if (this.selectedFaction) {
        this.factionService.updateFaction(this.selectedFaction.id, faction).subscribe(() => {
          this.loadFactions();
          this.closeFactionForm();
        });
      } else {
        this.factionService.createFaction(faction).subscribe(() => {
          this.loadFactions();
          this.closeFactionForm();
        });
      }
    }
  }

  deleteFaction(id: number, event?: Event) {
    event?.stopPropagation();

    if (confirm('Are you sure you want to delete this faction?')) {
      this.factionService.deleteFaction(id).subscribe(() => {
        this.loadFactions();
      });
    }
  }
}
