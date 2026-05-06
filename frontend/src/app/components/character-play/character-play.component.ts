import {Component, ElementRef, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, ViewChild, AfterViewInit, HostListener, NgZone, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {MatMenuModule} from '@angular/material/menu';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import {ItemStatsDialogComponent} from '../item-stats-dialog/item-stats-dialog.component';
import {MacroEditDialogComponent} from '../macro-edit-dialog/macro-edit-dialog.component';
import {StoreDialogComponent} from '../store-dialog/store-dialog.component';
import {MobileMacro} from '../../models/mobile-macro.model';
import {CharacterService} from '../../services/character.service';
import {GameWebSocketService} from '../../services/game-websocket.service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-character-play',
  standalone: true,
  imports: [CommonModule, FormsModule, DragDropModule, MatMenuModule, MatButtonModule, MatIconModule, MatDialogModule],
  templateUrl: './character-play.component.html',
  styleUrl: './character-play.component.css'
})
export class CharacterPlayComponent implements OnInit, OnChanges, OnDestroy, AfterViewInit {
  @Input() character: any;
  @Input() textMessages: string[] = [];
  @ViewChild('consoleTextarea') consoleTextarea!: ElementRef<HTMLTextAreaElement>;
  activeStatTab: string = 'stats';
  sortColumn: string = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';
  command: string = '';
  target: any = null;
  partyData: any = null;
  macros: MobileMacro[] = Array(12).fill(null).map((_, i) => ({ macroIndex: i, label: '', command: '' }));

  // Filter state
  filterName: string = '';
  filterType: string = '';
  filterLocation: string = '';
  filterMinValue: number | null = null;
  filterMaxValue: number | null = null;

  itemTypes: string[] = ['Weapon', 'Two Handed Weapon', 'Ranged Weapon', 'Light Armor', 'Medium Armor', 'Heavy Armor', 'Food', 'Drink', 'Potion', 'Book', 'Scroll', 'Money', 'Wand', 'Quest', 'Key', 'Light', 'Container', 'Trash', 'Miscellaneous', 'Corpse', 'Bandage'];
  wearLocations: string[] = ['Head', 'Neck', 'Torso', 'Arms', 'Hands', 'Finger', 'Waist', 'Legs', 'Feet', 'Wield', 'Hold', 'Shield', 'Floating', 'Light'];

  private wsSubscription: Subscription | null = null;
  private combatLogSubscription: Subscription | null = null;

  hp = signal(0);
  maxHp = signal(0);
  mana = signal(0);
  maxMana = signal(0);
  stamina = signal(0);
  maxStamina = signal(0);

  @HostListener('window:keydown', ['$event'])
  handleKeyDown(event: KeyboardEvent) {
    if (event.key.startsWith('F')) {
      const fNumber = parseInt(event.key.substring(1), 10);
      if (fNumber >= 1 && fNumber <= 12) {
        event.preventDefault(); // Override browser default
        this.triggerMacro(fNumber - 1);
      }
    }
  }

  constructor(
    private characterService: CharacterService,
    private gameWebSocketService: GameWebSocketService,
    private dialog: MatDialog,
    private ngZone: NgZone
  ) {
  }

  get sortedInventory() {
    if (!this.character || !this.character.inventory) return [];

    let inventory = [...this.character.inventory];

    inventory = inventory.filter(item => {
      if (this.filterName && !item.name?.toLowerCase().includes(this.filterName.toLowerCase())) return false;
      if (this.filterType && item.itemType !== this.filterType) return false;
      if (this.filterLocation && item.wearLocation !== this.filterLocation) return false;
      if (this.filterMinValue !== null && (item.value || 0) < this.filterMinValue) return false;
      if (this.filterMaxValue !== null && (item.value || 0) > this.filterMaxValue) return false;
      return true;
    });

    if (this.sortColumn) {
      inventory.sort((a, b) => {
        let valA = this.getSortValue(a, this.sortColumn);
        let valB = this.getSortValue(b, this.sortColumn);

        if (valA < valB) return this.sortDirection === 'asc' ? -1 : 1;
        if (valA > valB) return this.sortDirection === 'asc' ? 1 : -1;
        return 0;
      });
    }

    const flatten = (items: any[], level: number): any[] => {
      let flat: any[] = [];
      for (const item of items) {
        flat.push({ ...item, indentLevel: level });
        if (item.inventory && item.inventory.length > 0) {
          // Sort nested items by name by default to keep them organized
          let nested = [...item.inventory].sort((a, b) => (a.name || '').localeCompare(b.name || ''));
          flat = flat.concat(flatten(nested, level + 1));
        }
      }
      return flat;
    };

    return flatten(inventory, 0);
  }

  get sortedSkills() {
    if (!this.character || !this.character.skills) return [];
    return [...this.character.skills].sort((a, b) =>
      (a.name?.toLowerCase() || '').localeCompare(b.name?.toLowerCase() || '')
    );
  }

  ngOnInit() {
    if (this.character) {
      this.refreshCharacter();
      this.subscribeToUpdates();
    }
  }

  ngAfterViewInit() {
    this.scrollToBottom();
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
    if (this.combatLogSubscription) {
      this.combatLogSubscription.unsubscribe();
    }
  }

  private updateSignals() {
    if (this.character) {
      this.hp.set(this.character.currentHp || 0);
      this.maxHp.set(this.character.maxHp || 0);
      this.mana.set(this.character.currentMana || 0);
      this.maxMana.set(this.character.maxMana || 0);
      this.stamina.set(this.character.currentStamina || 0);
      this.maxStamina.set(this.character.maxStamina || 0);
    }
  }

  refreshCharacter() {
    if (!this.character?.id) return;
    this.characterService.getCharacter(this.character.id).subscribe({
      next: (c) => {
        Object.assign(this.character, c);
        this.updateSignals();
        this.loadMacros();
      },
      error: (err) => console.error('Error refreshing character', err)
    });
  }

  loadMacros() {
    if (!this.character?.id) return;
    this.characterService.getMacros(this.character.id).subscribe({
      next: (macros) => {
        // Overlay existing configuration onto our initialized 12-slot array
        if (macros && macros.length > 0) {
           macros.forEach(macro => {
             if (macro.macroIndex >= 0 && macro.macroIndex < 12) {
               this.macros[macro.macroIndex] = { ...macro };
             }
           });
        }
      },
      error: (err) => console.error('Error loading macros', err)
    });
  }

  saveMacros() {
    if (!this.character?.id) return;
    // Only save those that have actual commands or labels
    const macrosToSave = this.macros.filter(m => m.label || m.command);
    this.characterService.saveMacros(this.character.id, macrosToSave).subscribe({
      error: (err) => console.error('Error saving macros', err)
    });
  }

  triggerMacro(index: number) {
    const macro = this.macros[index];
    if (macro && macro.command) {
      this.characterService.sendCommand(this.character.id, macro.command).subscribe();
    }
  }

  editMacro(index: number, event: MouseEvent) {
    event.preventDefault(); // Stop normal context menu
    const macro = this.macros[index];
    
    // Open Dialog
    const dialogRef = this.dialog.open(MacroEditDialogComponent, {
      width: '300px',
      data: { ...macro } // Pass a copy to avoid immediate bindings if cancelled
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
         this.macros[index] = result;
         this.saveMacros();
      }
    });
  }

  subscribeToUpdates() {
    if (!this.character?.id) return;
    this.wsSubscription = this.gameWebSocketService.getCharacterUpdates(this.character.id).subscribe({
      next: (update) => {
        this.ngZone.run(() => {
          if (update.type === 'character') {
            // Apply updates by reassigning to trigger change detection accurately
            this.character = { ...this.character, ...update.data };
            this.updateSignals();
          } else if (update.type === 'text') {
            this.textMessages.push(update.data);
            this.scrollToBottom();
          } else if (update.type === 'target') {
            this.target = update.data;
          } else if (update.type === 'party') {
            this.partyData = update.data;
          } else if (update.type === 'storeDialog') {
            console.log('Received storeDialog WS payload:', update);
            this.dialog.open(StoreDialogComponent, {
              width: '1000px',
              height: '650px',
              maxWidth: '95vw',
              data: { storeId: update.data.storeId, characterId: update.data.characterId }
            });
          }
        });
      },
      error: (err) => console.error('WebSocket error', err)
    });

    this.combatLogSubscription = this.gameWebSocketService.getCombatLogs().subscribe({
      next: (update) => {
        this.ngZone.run(() => {
          if (update && update.message) {
             this.textMessages.push(update.message);
             this.scrollToBottom();
          }
        });
      },
      error: (err) => console.error('Combat Log WebSocket error', err)
    });
  }

  scrollToBottom() {
    setTimeout(() => {
      if (this.consoleTextarea) {
        const textarea = this.consoleTextarea.nativeElement;
        textarea.scrollTop = textarea.scrollHeight;
      }
    }, 50);
  }

  onSendCommand() {
    if (!this.command.trim() || !this.character?.id) return;

    const cmd = this.command.trim();
    this.command = '';

    this.characterService.sendCommand(this.character.id, cmd).subscribe({
      error: (err) => console.error('Error sending command', err)
    });
  }

  onRemovePartyMember(memberName: string) {
    if (!this.character?.id) return;
    this.characterService.sendCommand(this.character.id, 'party remove ' + memberName).subscribe({
      error: (err) => console.error('Error removing party member', err)
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

  onUnequip(slot: string) {
    if (!this.character?.id) return;
    this.characterService.unequipItem(this.character.id, slot).subscribe({
      next: (updatedChar) => {
        Object.assign(this.character, updatedChar);
      },
      error: (err) => console.error('Error unequipping item', err)
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
      data: {item},
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

  private getSortValue(item: any, column: string): any {
    switch (column) {
      case 'name':
        return item.name?.toLowerCase() || '';
      case 'count':
        return item.stackable ? (item.count || 1) : 1;
      case 'itemType':
        return item.itemType?.toLowerCase() || '';
      case 'wearLocation':
        return item.wearLocation?.toLowerCase() || '';
      case 'value':
        return item.value || 0;
      default:
        return '';
    }
  }
}
