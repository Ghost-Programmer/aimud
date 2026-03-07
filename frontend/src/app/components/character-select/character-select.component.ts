import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CharacterService } from '../../services/character.service';

@Component({
  selector: 'app-character-select',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './character-select.component.html',
  styleUrl: './character-select.component.css'
})
export class CharacterSelectComponent implements OnInit {
  @Output() characterSelected = new EventEmitter<any>();
  characters: any[] = [];

  constructor(private characterService: CharacterService) {}

  ngOnInit() {
    this.loadCharacters();
  }

  loadCharacters() {
    this.characterService.getCharacters().subscribe({
      next: (data) => {
        this.characters = data;
      },
      error: (error) => {
        console.error('Error fetching characters', error);
      }
    });
  }

  selectCharacter(character: any) {
    this.characterSelected.emit(character);
  }
}
