import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { RegisterDialogComponent } from './components/register-dialog/register-dialog.component';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, RegisterDialogComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'frontend';
  backendStatus = '';
  backendVersion = '';
  backendUptime = '';
  backendDatabase = '';

  serverName = 'AI Mud';
  allowNewUser = true;
  maintenance = false;
  maintenanceText = 'Undergoing Maintenance';
  settingsLoaded = false;
  isRegisterDialogOpen = false;

  openRegisterDialog() {
    this.isRegisterDialogOpen = true;
  }

  closeRegisterDialog() {
    this.isRegisterDialogOpen = false;
  }

  constructor(private http: HttpClient) { }

  ngOnInit() {
    this.http.get<{ status: string, version: string, uptime: string, database: string }>('/api/status').subscribe({
      next: (data) => {
        this.backendStatus = data.status;
        this.backendVersion = data.version;
        this.backendUptime = data.uptime;
        this.backendDatabase = data.database;
      },
      error: (error) => {
        console.error('Error fetching backend status:', error);
        this.backendStatus = 'OFFLINE';
        this.backendDatabase = 'Disconnected';
      }
    });

    this.http.get<{ serverName: string, allowNewUser: boolean, maintenance: boolean, maintenanceText: string }>('/api/settings').subscribe({
      next: (data) => {
        this.serverName = data.serverName;
        this.allowNewUser = data.allowNewUser;
        this.maintenance = data.maintenance;
        this.maintenanceText = data.maintenanceText;
        this.settingsLoaded = true;
      },
      error: (error) => {
        console.error('Error fetching server settings:', error);
        this.settingsLoaded = true;
      }
    });
  }
}
