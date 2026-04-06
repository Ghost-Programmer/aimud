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

  getAllFactions(): Observable<Faction[]> {
    return this.http.get<Faction[]>(this.apiUrl);
  }

  getMobileFactionRatings(mobileId: number): Observable<{ [key: number]: number }> {
    return this.http.get<{ [key: number]: number }>(`${this.apiUrl}/mobile/${mobileId}`);
  }

  updateMobileFactionRatings(mobileId: number, ratings: { [key: number]: number }): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/mobile/${mobileId}`, ratings);
  }
}
