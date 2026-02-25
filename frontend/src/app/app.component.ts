import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'frontend';
  backendStatus = '';
  backendVersion = '';
  backendUptime = '';
  backendDatabase = '';

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
  }
}
