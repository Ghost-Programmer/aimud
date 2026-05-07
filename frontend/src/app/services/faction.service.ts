import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Faction } from '../models/faction.model';

@Injectable({
  providedIn: 'root'
})
export class FactionService {
  private apiUrl = '/api/factions';

  constructor(private http: HttpClient) {}

  getAllFactions(page: number = 0, size: number = 10): Observable<any> {
    return this.http.get<any>(this.apiUrl, { params: { page, size } });
  }

  getMobileFactionRatings(mobileId: number): Observable<{ [key: number]: number }> {
    return this.http.get<{ [key: number]: number }>(`${this.apiUrl}/mobile/${mobileId}`);
  }

  updateMobileFactionRatings(mobileId: number, ratings: { [key: number]: number }): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/mobile/${mobileId}`, ratings);
  }

  createFaction(faction: Faction): Observable<Faction> {
    return this.http.post<Faction>(this.apiUrl, faction);
  }

  updateFaction(id: number, faction: Faction): Observable<Faction> {
    return this.http.put<Faction>(`${this.apiUrl}/${id}`, faction);
  }

  deleteFaction(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
