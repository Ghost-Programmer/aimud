import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { CharacterService } from '../../services/character.service';

@Component({
  selector: 'app-character-play',
  standalone: true,
  imports: [CommonModule, DragDropModule],
  templateUrl: './character-play.component.html',
  styleUrl: './character-play.component.css'
})
export class CharacterPlayComponent implements OnInit, OnChanges {
  @Input() character: any;
  activeStatTab: string = 'stats';
  currentRoom: any = null;
  exits: string[] = [];

  constructor(private characterService: CharacterService) {}

  ngOnInit() {
    if (this.character) {
      this.loadRoomData();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['character'] && !changes['character'].firstChange) {
      this.loadRoomData();
    }
  }

  loadRoomData() {
    if (!this.character || !this.character.id) return;

    this.characterService.getCharacterRoom(this.character.id).subscribe({
      next: (room) => {
        this.currentRoom = room;
        this.parseExits(room);
      },
      error: (error) => {
        console.error('Error loading room data', error);
      }
    });
  }

  parseExits(room: any) {
    this.exits = [];
    if (room.northId) this.exits.push('North');
    if (room.southId) this.exits.push('South');
    if (room.eastId) this.exits.push('East');
    if (room.westId) this.exits.push('West');
    if (room.upId) this.exits.push('Up');
    if (room.downId) this.exits.push('Down');
  }

  setActiveStatTab(tab: string) {
    this.activeStatTab = tab;
  }
}
