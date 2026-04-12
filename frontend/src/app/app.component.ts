import {Component, OnInit, OnDestroy} from '@angular/core';
import {Router, RouterOutlet} from '@angular/router';
import {RegisterDialogComponent} from './components/register-dialog/register-dialog.component';
import {LoginDialogComponent} from './components/login-dialog/login-dialog.component';
import {HttpClient} from '@angular/common/http';
import {CommonModule} from '@angular/common';
import {Title} from '@angular/platform-browser';
import {StatusService, SystemStatus} from './services/status.service';
import {Subscription, interval} from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, RegisterDialogComponent, LoginDialogComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'frontend';
  systemStatus?: SystemStatus;

  serverName = 'AI Mud';
  allowNewUser = true;
  maintenance = false;
  maintenanceText = 'Undergoing Maintenance';
  settingsLoaded = false;
  isRegisterDialogOpen = false;
  isLoginDialogOpen = false;

  private statusSubscription?: Subscription;

  constructor(
    private http: HttpClient,
    public router: Router,
    private statusService: StatusService,
    private titleService: Title
  ) {
  }

  ngOnDestroy() {
    if (this.statusSubscription) {
      this.statusSubscription.unsubscribe();
    }
  }

  openRegisterDialog() {
    this.isRegisterDialogOpen = true;
  }

  closeRegisterDialog() {
    this.isRegisterDialogOpen = false;
  }

  openLoginDialog() {
    this.isLoginDialogOpen = true;
  }

  closeLoginDialog() {
    this.isLoginDialogOpen = false;
  }

  ngOnInit() {
    this.titleService.setTitle(this.serverName);

    this.fetchSystemStatus();
    
    // Poll every 60 seconds (60000 ms) only when on the main page
    this.statusSubscription = interval(60000).subscribe(() => {
      if (this.router.url === '/') {
        this.fetchSystemStatus();
      }
    });

    this.http.get<{
      serverName: string,
      allowNewUser: boolean,
      maintenance: boolean,
      maintenanceText: string
    }>('/api/settings').subscribe({
      next: (data) => {
        this.serverName = data.serverName;
        this.titleService.setTitle(this.serverName || 'AI Mud');
        this.allowNewUser = data.allowNewUser;
        this.maintenance = data.maintenance;
        this.maintenanceText = data.maintenanceText;
        this.settingsLoaded = true;
      },
      error: (error) => {
        console.error('Error fetching server settings:', error);
        this.titleService.setTitle(this.serverName || 'AI Mud');
        this.settingsLoaded = true;
      }
    });
  }

  private fetchSystemStatus() {
    this.statusService.getSystemStatus().subscribe({
      next: (data) => {
        this.systemStatus = data;
      },
      error: (error) => {
        console.error('Error fetching backend status:', error);
        this.systemStatus = {
          status: 'OFFLINE',
          version: '---',
          uptime: '---',
          database: 'Disconnected',
          llmStatus: 'Disconnected',
          llmModel: '---'
        };
      }
    });
  }
}
