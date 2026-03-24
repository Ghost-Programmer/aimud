import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CharacterService } from '../../services/character.service';
import { ConfigService } from '../../services/config.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-character-select',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './character-select.component.html',
  styleUrl: './character-select.component.css'
})
export class CharacterSelectComponent implements OnInit {
  @Input() excludeCharacterIds: number[] = [];
  @Output() characterSelected = new EventEmitter<any>();
  characters: any[] = [];
  races: any[] = [];
  classes: any[] = [];

  constructor(
    private characterService: CharacterService,
    private configService: ConfigService
  ) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    forkJoin({
      characters: this.characterService.getCharacters(),
      races: this.configService.getAllRaces(),
      classes: this.configService.getAllCharacterClasses()
    }).subscribe({
      next: ({ characters, races, classes }) => {
        this.characters = [...characters].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''));
        this.races = [...races].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''));
        this.classes = [...classes].sort((a, b) => (a.name ?? '').localeCompare(b.name ?? ''));
      },
      error: (error) => {
        console.error('Error loading data', error);
      }
    });
  }

  get filteredCharacters() {
    return this.characters.filter(c => !this.excludeCharacterIds.includes(c.id));
  }

  selectCharacter(character: any) {
    this.characterService.selectCharacter(character.id).subscribe({
      next: () => {
        this.characterSelected.emit(character);
      },
      error: (err) => {
        console.error('Error selecting character', err);
      }
    });
  }

  getRaceName(raceId: number): string {
    const race = this.races.find(r => r.id === raceId);
    return race ? race.name : 'Unknown';
  }

  getClassName(classId: number): string {
    const cls = this.classes.find(c => c.id === classId);
    return cls ? cls.name : 'Unknown';
  }
}
