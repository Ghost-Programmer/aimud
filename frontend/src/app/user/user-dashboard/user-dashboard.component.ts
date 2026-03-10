import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DragDropModule, CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { PlayerListComponent } from '../../components/player-list/player-list.component';
import { CreateCharacterComponent } from '../../components/create-character/create-character.component';
import { CharacterSelectComponent } from '../../components/character-select/character-select.component';
import { CharacterPlayComponent } from '../../components/character-play/character-play.component';
import { ConfigDashboardComponent } from '../../components/config-dashboard/config-dashboard.component';
import { ItemCreatorComponent } from '../../components/item-creator/item-creator.component';
import { RoomManagementComponent } from '../../components/room-management/room-management.component';
import { AiDialogComponent } from '../../components/ai-dialog/ai-dialog.component';

interface Tab {
  id: string;
  label: string;
  type: 'create-character' | 'select-character' | 'play-character' | 'players' | 'config' | 'world';
  data?: any;
}

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, DragDropModule, PlayerListComponent, CreateCharacterComponent, CharacterSelectComponent, CharacterPlayComponent, ConfigDashboardComponent, ItemCreatorComponent, RoomManagementComponent, AiDialogComponent],
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.css'
})
export class UserDashboardComponent implements OnInit, OnDestroy {
  role: string = '';
  localTime: string = '';
  onlineTime: string = '00:00:00';
  isAiDialogOpen: boolean = false;

  tabs: Tab[] = [];
  activeTabId: string = '';
  activeWorldSection: string = '';

  private loginTime: number = Date.now();
  private timerInterval: any;

  constructor(private router: Router) {}

  ngOnInit() {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.role = payload.role;

        // Initialize tabs based on role
        if (this.role === 'MUD_ADMIN') {
          this.tabs.push({ id: 'world', label: 'World', type: 'world' });
          this.tabs.push({ id: 'players', label: 'Players', type: 'players' });
          this.tabs.push({ id: 'config', label: 'Config', type: 'config' });
        }
        this.tabs.push({ id: 'select-character-1', label: 'Select Character', type: 'select-character' });
        this.tabs.push({ id: 'create-character', label: 'Create Character', type: 'create-character' });

        // Set active tab to the first one
        if (this.tabs.length > 0) {
          this.activeTabId = this.tabs[0].id;
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

  toggleWorldSection(sectionId: string) {
    if (this.activeWorldSection === sectionId) {
      this.activeWorldSection = '';
    } else {
      this.activeWorldSection = sectionId;
    }
  }

  onTabDrop(event: CdkDragDrop<Tab[]>) {
    moveItemInArray(this.tabs, event.previousIndex, event.currentIndex);
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

      // Add a new Select Character tab if one doesn't exist
      const hasSelectTab = this.tabs.some(t => t.type === 'select-character');
      if (!hasSelectTab) {
        const newSelectTabId = `select-character-${Date.now()}`;
        // Insert it after the current tab
        const newTab: Tab = { id: newSelectTabId, label: 'Select Character', type: 'select-character' };
        this.tabs.splice(tabIndex + 1, 0, newTab);
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

  openAiDialog() {
    this.isAiDialogOpen = true;
  }

  closeAiDialog() {
    this.isAiDialogOpen = false;
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
