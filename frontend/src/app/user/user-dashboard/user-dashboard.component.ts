import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PlayerListComponent } from '../../components/player-list/player-list.component';
import { CreateCharacterComponent } from '../../components/create-character/create-character.component';
import { CharacterSelectComponent } from '../../components/character-select/character-select.component';
import { CharacterPlayComponent } from '../../components/character-play/character-play.component';

interface Tab {
  id: string;
  label: string;
  type: 'create-character' | 'select-character' | 'play-character' | 'players';
  data?: any;
}

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, PlayerListComponent, CreateCharacterComponent, CharacterSelectComponent, CharacterPlayComponent],
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.css'
})
export class UserDashboardComponent implements OnInit, OnDestroy {
  role: string = '';
  localTime: string = '';
  onlineTime: string = '00:00:00';

  tabs: Tab[] = [
    { id: 'create-character', label: 'Create Character', type: 'create-character' },
    { id: 'select-character-1', label: 'Select Character', type: 'select-character' }
  ];
  activeTabId: string = 'create-character';

  private loginTime: number = Date.now();
  private timerInterval: any;

  constructor(private router: Router) {}

  ngOnInit() {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.role = payload.role;
        if (this.role === 'MUD_ADMIN') {
          this.tabs.push({ id: 'players', label: 'Players', type: 'players' });
        }
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

  setActiveTab(tabId: string) {
    this.activeTabId = tabId;
  }

  onCharacterSelected(character: any, tabId: string) {
    // Find the tab that triggered this
    const tabIndex = this.tabs.findIndex(t => t.id === tabId);
    if (tabIndex !== -1) {
      // Update the current tab to be a play tab
      this.tabs[tabIndex] = {
        id: `play-${character.id}`,
        label: character.name,
        type: 'play-character',
        data: character
      };
      this.activeTabId = this.tabs[tabIndex].id;

      // Add a new Select Character tab if one doesn't exist (optional, but requested behavior implies always having one available)
      // Check if there is already a generic select character tab
      const hasSelectTab = this.tabs.some(t => t.type === 'select-character');
      if (!hasSelectTab) {
        const newSelectTabId = `select-character-${Date.now()}`;
        // Insert it after the current tab or at the end? Let's put it after the create tab for consistency or just append.
        // Let's append it before the Players tab if it exists.
        const playersTabIndex = this.tabs.findIndex(t => t.type === 'players');
        const newTab: Tab = { id: newSelectTabId, label: 'Select Character', type: 'select-character' };

        if (playersTabIndex !== -1) {
          this.tabs.splice(playersTabIndex, 0, newTab);
        } else {
          this.tabs.push(newTab);
        }
      }
    }
  }

  closeTab(tabId: string, event: Event) {
    event.stopPropagation();
    const tabIndex = this.tabs.findIndex(t => t.id === tabId);
    if (tabIndex !== -1) {
      this.tabs.splice(tabIndex, 1);
      // If we closed the active tab, switch to the previous one
      if (this.activeTabId === tabId) {
        this.activeTabId = this.tabs[Math.max(0, tabIndex - 1)].id;
      }
    }
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

  getActiveTab(): Tab | undefined {
    return this.tabs.find(t => t.id === this.activeTabId);
  }

  getPlayingCharacterIds(): number[] {
    return this.tabs
      .filter(t => t.type === 'play-character')
      .map(t => t.data.id);
  }
}
