import { Component, OnInit, OnChanges, Input, SimpleChanges, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ItemStatsDialogComponent } from '../item-stats-dialog/item-stats-dialog.component';
import { CharacterService } from '../../services/character.service';
import { GameWebSocketService } from '../../services/game-websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-character-play',
  standalone: true,
  imports: [CommonModule, FormsModule, DragDropModule, MatMenuModule, MatButtonModule, MatIconModule, MatDialogModule],
  templateUrl: './character-play.component.html',
  styleUrl: './character-play.component.css'
})
export class CharacterPlayComponent implements OnInit, OnChanges, OnDestroy {
  @Input() character: any;
  @Input() textMessages: string[] = [];
  @ViewChild('consoleTextarea') consoleTextarea!: ElementRef<HTMLTextAreaElement>;
  activeStatTab: string = 'stats';
  sortColumn: string = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';
  command: string = '';
  target: any = null;

  private wsSubscription: Subscription | null = null;

  constructor(
      private characterService: CharacterService,
      private gameWebSocketService: GameWebSocketService,
      private dialog: MatDialog
  ) {}

  ngOnInit() {
    // Create a shallow copy of the messages to prevent mutations of the parent array.
    // This ensures that messages for one character don't leak to another.
    this.textMessages = [...this.textMessages];
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
              } else if (update.type === 'target') {
                  this.target = update.data;
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

  onEquip(itemId: number) {
      if (!this.character?.id) return;
      this.characterService.equipItem(this.character.id, itemId).subscribe({
          next: (updatedChar) => {
              Object.assign(this.character, updatedChar);
          },
          error: (err) => console.error('Error equipping item', err)
      });
  }

  onDrop(itemId: number) {
      if (!this.character?.id) return;
      this.characterService.dropItem(this.character.id, itemId).subscribe({
          next: (updatedChar) => {
              Object.assign(this.character, updatedChar);
          },
          error: (err) => console.error('Error dropping item', err)
      });
  }

  onShowItemStats(item: any) {
    if (!item) return;
    this.dialog.open(ItemStatsDialogComponent, {
      data: { item },
      width: '320px',
      maxWidth: '90vw'
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

  get sortedSkills() {
    if (!this.character || !this.character.skills) return [];
    return [...this.character.skills].sort((a, b) =>
      (a.name?.toLowerCase() || '').localeCompare(b.name?.toLowerCase() || '')
    );
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
