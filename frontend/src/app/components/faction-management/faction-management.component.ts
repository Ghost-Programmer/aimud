import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfigService } from '../../services/config.service';

@Component({
  selector: 'app-faction-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './faction-management.component.html',
  styleUrl: './faction-management.component.css'
})
export class FactionManagementComponent implements OnInit {
  factions: any[] = [];
  selectedFaction: any = null;
  factionForm: FormGroup;
  isFactionFormVisible = false;

  constructor(
    private fb: FormBuilder,
    private configService: ConfigService
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

  loadFactions() {
    this.configService.getAllFactions().subscribe(factions => {
      this.factions = [...factions].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''));
    });
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
        this.configService.updateFaction(this.selectedFaction.id, faction).subscribe(() => {
          this.loadFactions();
          this.closeFactionForm();
        });
      } else {
        this.configService.createFaction(faction).subscribe(() => {
          this.loadFactions();
          this.closeFactionForm();
        });
      }
    }
  }

  deleteFaction(id: number, event?: Event) {
    event?.stopPropagation();

    if (confirm('Are you sure you want to delete this faction?')) {
      this.configService.deleteFaction(id).subscribe(() => {
        this.loadFactions();
      });
    }
  }
}
