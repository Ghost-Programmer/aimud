import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PlayerListComponent } from '../../components/player-list/player-list.component';
import { CreateCharacterComponent } from '../../components/create-character/create-character.component';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, PlayerListComponent, CreateCharacterComponent],
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.css'
})
export class UserDashboardComponent implements OnInit, OnDestroy {
  role: string = '';
  localTime: string = '';
  onlineTime: string = '00:00:00';
  activeTab: string = 'create-character';
  private loginTime: number = Date.now();
  private timerInterval: any;

  constructor(private router: Router) {}

  ngOnInit() {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.role = payload.role;
      } catch (e) {
        console.error('Error parsing token', e);
      }
    }

    this.updateTime();
    this.timerInterval = setInterval(() => {
      this.updateTime();
    }, 1000);
  }

  ngOnDestroy() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  setActiveTab(tabName: string) {
    this.activeTab = tabName;
  }

  logout() {
    localStorage.removeItem('token');
    this.router.navigate(['/']);
  }

  private updateTime() {
    const now = new Date();
    this.localTime = now.toLocaleTimeString();

    const diff = Math.floor((now.getTime() - this.loginTime) / 1000);
    const hours = Math.floor(diff / 3600);
    const minutes = Math.floor((diff % 3600) / 60);
    const seconds = diff % 60;

    this.onlineTime = `${this.pad(hours)}:${this.pad(minutes)}:${this.pad(seconds)}`;
  }

  private pad(num: number): string {
    return num < 10 ? '0' + num : num.toString();
  }
}
