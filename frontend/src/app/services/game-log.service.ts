import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GameLog {
  id?: number;
  mobileId?: number;
  isWorldLog: boolean;
  message: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class GameLogService {
  private apiUrl = '/api/logs';

  constructor(private http: HttpClient) { }

  getPersonalLogs(mobileId: number): Observable<GameLog[]> {
    return this.http.get<GameLog[]>(`${this.apiUrl}/personal?mobileId=${mobileId}`);
  }

  getWorldLogs(): Observable<GameLog[]> {
    return this.http.get<GameLog[]>(`${this.apiUrl}/world`);
  }
}
