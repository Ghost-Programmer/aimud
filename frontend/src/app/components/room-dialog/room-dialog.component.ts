import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Room, RoomType} from '../../models/room.model';
import {Item} from '../../models/item.model';
import {ItemService} from '../../services/item.service';
import {SearchableDropdownComponent} from '../searchable-dropdown/searchable-dropdown.component';

@Component({
  selector: 'app-room-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, SearchableDropdownComponent],
  templateUrl: './room-dialog.component.html',
  styleUrl: './room-dialog.component.css'
})
export class RoomDialogComponent implements OnInit {
  @Input() room?: Room;
  @Output() save = new EventEmitter<Room>();
  @Output() cancel = new EventEmitter<void>();

  editRoom: Room = {
    name: '',
    description: '',
    roomType: RoomType.INDOORS,
    northDoor: false,
    southDoor: false,
    eastDoor: false,
    westDoor: false,
    upDoor: false,
    downDoor: false,
    northDoorOpen: true,
    southDoorOpen: true,
    eastDoorOpen: true,
    westDoorOpen: true,
    upDoorOpen: true,
    downDoorOpen: true,
    items: ''
  };

  roomTypes = Object.values(RoomType);

  availableItems: Item[] = [];
  selectedItemId: number | null = null;
  currentRoomItems: Item[] = [];

  itemDisplayFn = (item: Item) => `${item.name} (${item.itemType})`;

  constructor(private itemService: ItemService) {
  }

  ngOnInit(): void {
    this.loadItems();
    if (this.room) {
      this.editRoom = JSON.parse(JSON.stringify(this.room));
    }
  }

  loadItems() {
    this.itemService.getItems(0, 1000, {}).subscribe(response => {
      this.availableItems = response.items.sort((a, b) => (a.name || '').localeCompare(b.name || ''));
      this.initializeRoomItems();
    });
  }

  initializeRoomItems() {
    if (this.editRoom.items) {
      const itemIds = this.editRoom.items.split(',')
        .map((s: string) => parseInt(s.trim()))
        .filter((n: number) => !isNaN(n));

      this.currentRoomItems = this.availableItems.filter(i => i.id !== undefined && itemIds.includes(i.id));
    }
  }

  addSelectedItem() {
    if (this.selectedItemId) {
      const itemId = Number(this.selectedItemId);
      const itemToAdd = this.availableItems.find(i => i.id === itemId);
      if (itemToAdd) {
        this.currentRoomItems.push(itemToAdd);
        this.updateRoomItemsString();
      }
      this.selectedItemId = null;
    }
  }

  removeItem(index: number) {
    this.currentRoomItems.splice(index, 1);
    this.updateRoomItemsString();
  }

  updateRoomItemsString() {
    this.editRoom.items = this.currentRoomItems.map(i => i.id).join(',');
  }

  onSelectItemIdChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.selectedItemId = target.value ? Number(target.value) : null;
  }

  onSave() {
    this.save.emit(this.editRoom);
  }

  onCancel() {
    this.cancel.emit();
  }
}
