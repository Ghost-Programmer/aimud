import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

@Component({
    selector: 'app-home',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './home.component.html',
    styleUrls: ['../app.component.css', './home.component.css']
})
export class HomeComponent implements OnInit {
    backendStatus = '';
    backendVersion = '';
    backendUptime = '';
    backendDatabase = '';

    serverName = 'AI Mud';
    allowNewUser = true;
    maintenance = false;
    maintenanceText = 'Undergoing Maintenance';
    settingsLoaded = false;

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
                this.backendStatus = 'OFFERS';
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
