import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RoomService } from '../../services/room.service';
import { Room, RoomType } from '../../models/room.model';
import { RoomDialogComponent } from '../room-dialog/room-dialog.component';

@Component({
  selector: 'app-room-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RoomDialogComponent],
  templateUrl: './room-management.component.html',
  styleUrl: './room-management.component.css'
})
export class RoomManagementComponent implements OnInit {
  rooms: Room[] = [];
  totalRooms: number = 0;
  page: number = 0;
  size: number = 10;

  showDialog = false;
  selectedRoom: Room | undefined = undefined;

  filters = {
    name: '',
    type: '' as RoomType | '',
    minId: undefined as number | undefined,
    maxId: undefined as number | undefined
  };

  roomTypes = Object.values(RoomType);

  constructor(
    private roomService: RoomService
  ) {}

  ngOnInit(): void {
    this.loadRooms();
  }

  get Math() {
    return Math;
  }

  loadRooms() {
    const activeFilters: any = {};
    if (this.filters.name) activeFilters.name = this.filters.name;
    if (this.filters.type) activeFilters.type = this.filters.type;
    if (this.filters.minId !== undefined) activeFilters.minId = this.filters.minId;
    if (this.filters.maxId !== undefined) activeFilters.maxId = this.filters.maxId;

    this.roomService.getRooms(this.page, this.size, activeFilters).subscribe(response => {
      this.rooms = response.rooms;
      this.totalRooms = response.total;
    });
  }

  onSearch() {
    this.page = 0;
    this.loadRooms();
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.loadRooms();
    }
  }

  nextPage() {
    if ((this.page + 1) * this.size < this.totalRooms) {
      this.page++;
      this.loadRooms();
    }
  }

  openCreateDialog() {
    this.selectedRoom = undefined;
    this.showDialog = true;
  }

  editRoom(room: Room) {
    this.selectedRoom = room;
    this.showDialog = true;
  }

  onSaveRoom(room: Room) {
    if (room.id) {
      this.roomService.updateRoom(room.id, room).subscribe(() => {
        this.showDialog = false;
        this.loadRooms();
      });
    } else {
      this.roomService.createRoom(room).subscribe(() => {
        this.showDialog = false;
        this.loadRooms();
      });
    }
  }

  onCancelDialog() {
    this.showDialog = false;
  }

  deleteRoom(room: Room, event: Event) {
    event.stopPropagation();
    if (confirm(`Are you sure you want to delete ${room.name}?`)) {
      this.roomService.deleteRoom(room.id!).subscribe(() => {
        this.loadRooms();
      });
    }
  }
}
