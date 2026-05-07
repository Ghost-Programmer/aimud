import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfigService } from '../../services/config.service';

@Component({
  selector: 'app-race-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './race-management.component.html',
  styleUrl: './race-management.component.css'
})
export class RaceManagementComponent implements OnInit {
  races: any[] = [];
  selectedRace: any = null;
  raceForm: FormGroup;
  isRaceFormVisible = false;

  constructor(
    private fb: FormBuilder,
    private configService: ConfigService
  ) {
    this.raceForm = this.fb.group({
      id: [null],
      name: ['', Validators.required],
      description: ['', Validators.required],
      npcOnly: [false],
      strengthMod: [0],
      intelligenceMod: [0],
      wisdomMod: [0],
      charismaMod: [0],
      dexterityMod: [0],
      constitutionMod: [0]
    });
  }

  ngOnInit() {
    this.loadRaces();
  }

  loadRaces() {
    this.configService.getAllRaces().subscribe(races => {
      this.races = [...races].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''));
    });
  }

  openRaceForm(race: any = null) {
    this.selectedRace = race;
    if (race) {
      this.raceForm.patchValue(race);
    } else {
      this.raceForm.reset({
        id: null,
        name: '',
        description: '',
        npcOnly: false,
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

  deleteRace(id: number, event?: Event) {
    event?.stopPropagation();

    if (confirm('Are you sure you want to delete this race?')) {
      this.configService.deleteRace(id).subscribe(() => {
        this.loadRaces();
      });
    }
  }
}
