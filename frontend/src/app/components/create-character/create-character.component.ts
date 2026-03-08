import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CharacterService } from '../../services/character.service';
import { ConfigService } from '../../services/config.service';

@Component({
  selector: 'app-create-character',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-character.component.html',
  styleUrl: './create-character.component.css'
})
export class CreateCharacterComponent implements OnInit {
  characterForm: FormGroup;
  stats: any = {
    strength: 0,
    dexterity: 0,
    constitution: 0,
    intelligence: 0,
    wisdom: 0,
    charisma: 0
  };
  races: any[] = [];
  classes: any[] = [];

  constructor(
    private fb: FormBuilder,
    private characterService: CharacterService,
    private configService: ConfigService
  ) {
    this.characterForm = this.fb.group({
      name: ['', Validators.required],
      raceId: [null, Validators.required],
      classId: [null, Validators.required]
    });
  }

  ngOnInit() {
    this.rollStats();
    this.loadRaces();
    this.loadClasses();
  }

  loadRaces() {
    this.configService.getAllRaces().subscribe(races => {
      this.races = races;
    });
  }

  loadClasses() {
    this.configService.getAllCharacterClasses().subscribe(classes => {
      this.classes = classes;
    });
  }

  rollStats() {
    this.stats = {
      strength: this.roll(),
      dexterity: this.roll(),
      constitution: this.roll(),
      intelligence: this.roll(),
      wisdom: this.roll(),
      charisma: this.roll()
    };
  }

  roll(): number {
    return Math.floor(Math.random() * 4) + 1;
  }

  saveCharacter() {
    if (this.characterForm.valid) {
      const character = {
        ...this.characterForm.value,
        ...this.stats
      };
      this.characterService.createCharacter(character).subscribe({
        next: (response) => {
          console.log('Character created', response);
          alert('Character created successfully!');
          this.characterForm.reset();
          this.rollStats();
        },
        error: (error) => {
          console.error('Error creating character', error);
          alert('Failed to create character');
        }
      });
    }
  }

  getSelectedRaceDescription(): string {
    const raceId = this.characterForm.get('raceId')?.value;
    const race = this.races.find(r => r.id === raceId);
    return race ? race.description : '';
  }

  getSelectedClassDescription(): string {
    const classId = this.characterForm.get('classId')?.value;
    const cls = this.classes.find(c => c.id === classId);
    return cls ? cls.description : '';
  }
}
