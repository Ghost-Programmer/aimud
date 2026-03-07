import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-player-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './player-list.component.html',
  styleUrl: './player-list.component.css'
})
export class PlayerListComponent implements OnInit {
  users: any[] = [];

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
      },
      error: (error) => {
        console.error('Error fetching users', error);
      }
    });
  }

  changePassword(userId: number) {
    const newPassword = prompt('Enter new password:');
    if (newPassword) {
      this.userService.changePassword(userId, newPassword).subscribe({
        next: () => {
          alert('Password changed successfully');
        },
        error: (error) => {
          console.error('Error changing password', error);
          alert('Failed to change password');
        }
      });
    }
  }

  toggleLock(user: any) {
    this.userService.toggleLock(user.id).subscribe({
      next: (updatedUser) => {
        user.locked = updatedUser.locked;
      },
      error: (error) => {
        console.error('Error toggling lock', error);
        alert('Failed to toggle lock status');
      }
    });
  }
}
