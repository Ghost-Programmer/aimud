import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Room, RoomType } from '../../models/room.model';

@Component({
  selector: 'app-room-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
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
    downDoorOpen: true
  };

  roomTypes = Object.values(RoomType);

  constructor() {}

  ngOnInit(): void {
    if (this.room) {
      this.editRoom = JSON.parse(JSON.stringify(this.room));
    }
  }

  onSave() {
    this.save.emit(this.editRoom);
  }

  onCancel() {
    this.cancel.emit();
  }
}
