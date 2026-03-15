import { Component, OnInit, OnChanges, Input, SimpleChanges, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { CharacterService } from '../../services/character.service';
import { GameWebSocketService } from '../../services/game-websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-character-play',
  standalone: true,
  imports: [CommonModule, FormsModule, DragDropModule],
  templateUrl: './character-play.component.html',
  styleUrl: './character-play.component.css'
})
export class CharacterPlayComponent implements OnInit, OnChanges, OnDestroy {
  @Input() character: any;
  @Input() textMessages: string[] = [];
  @ViewChild('consoleTextarea') consoleTextarea!: ElementRef<HTMLTextAreaElement>;
  activeStatTab: string = 'stats';
  sortColumn: string = '';
  sortDirection: 'asc' | 'desc' = 'asc';
  command: string = '';

  private wsSubscription: Subscription | null = null;

  constructor(
      private characterService: CharacterService,
      private gameWebSocketService: GameWebSocketService
  ) {}

  ngOnInit() {
    if (this.character) {
      this.refreshCharacter();
      this.subscribeToUpdates();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['character'] && !changes['character'].firstChange) {
      this.refreshCharacter();

      if (this.wsSubscription) {
          this.wsSubscription.unsubscribe();
      }
      this.subscribeToUpdates();
    }
  }

  ngOnDestroy() {
      if (this.wsSubscription) {
          this.wsSubscription.unsubscribe();
      }
  }

  refreshCharacter() {
      if (!this.character?.id) return;
      this.characterService.getCharacter(this.character.id).subscribe({
          next: (c) => {
              Object.assign(this.character, c);
          },
          error: (err) => console.error('Error refreshing character', err)
      });
  }

  subscribeToUpdates() {
      if (!this.character?.id) return;
      this.wsSubscription = this.gameWebSocketService.getCharacterUpdates(this.character.id).subscribe({
          next: (update) => {
              if (update.type === 'character') {
                  // Apply updates in place to maintain reference for parent
                  Object.assign(this.character, update.data);
              } else if (update.type === 'text') {
                  this.textMessages.push(update.data);
                  this.scrollToBottom();
              }
          },
          error: (err) => console.error('WebSocket error', err)
      });
  }

  scrollToBottom() {
      setTimeout(() => {
          if (this.consoleTextarea) {
              const textarea = this.consoleTextarea.nativeElement;
              textarea.scrollTop = textarea.scrollHeight;
          }
      }, 0);
  }

  onSendCommand() {
      if (!this.command.trim() || !this.character?.id) return;

      const cmd = this.command.trim();
      this.command = '';

      this.characterService.sendCommand(this.character.id, cmd).subscribe({
          error: (err) => console.error('Error sending command', err)
      });
  }

  setActiveStatTab(tab: string) {
    this.activeStatTab = tab;
  }

  toggleSort(column: string) {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
  }

  get sortedInventory() {
    if (!this.character || !this.character.inventory) return [];

    const inventory = [...this.character.inventory];
    if (!this.sortColumn) return inventory;

    return inventory.sort((a, b) => {
      let valA = this.getSortValue(a, this.sortColumn);
      let valB = this.getSortValue(b, this.sortColumn);

      if (valA < valB) return this.sortDirection === 'asc' ? -1 : 1;
      if (valA > valB) return this.sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }

  private getSortValue(item: any, column: string): any {
    switch (column) {
      case 'name': return item.name?.toLowerCase() || '';
      case 'itemType': return item.itemType?.toLowerCase() || '';
      case 'wearLocation': return item.wearLocation?.toLowerCase() || '';
      case 'value': return item.value || 0;
      default: return '';
    }
  }
}
