import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SystemStatus {
  status: string;
  database: string;
  version: string;
  uptime: string;
  llmStatus: string;
  llmModel: string;
}

@Injectable({
  providedIn: 'root'
})
export class StatusService {
  private apiUrl = '/api/status';

  constructor(private http: HttpClient) {}

  getSystemStatus(): Observable<SystemStatus> {
    return this.http.get<SystemStatus>(this.apiUrl);
  }
}
