import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfigService } from '../../services/config.service';

@Component({
  selector: 'app-config-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
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

  constructor(private fb: FormBuilder, private configService: ConfigService) {
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
      constitutionMod: [0]
    });
  }

  ngOnInit() {
    this.loadServerSettings();
    this.loadRaces();
    this.loadClasses();
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

  // Classes
  loadClasses() {
    this.configService.getAllCharacterClasses().subscribe(classes => {
      this.classes = classes;
    });
  }

  openClassForm(cls: any = null) {
    this.selectedClass = cls;
    if (cls) {
      this.classForm.patchValue(cls);
    } else {
      this.classForm.reset({
        strengthMod: 0, intelligenceMod: 0, wisdomMod: 0,
        charismaMod: 0, dexterityMod: 0, constitutionMod: 0
      });
    }
    this.isClassFormVisible = true;
  }

  closeClassForm() {
    this.isClassFormVisible = false;
    this.selectedClass = null;
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
}
