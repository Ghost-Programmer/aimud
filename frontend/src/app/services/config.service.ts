import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  private apiUrl = '/api/config';

  constructor(private http: HttpClient) { }

  // Server Settings
  getServerSettings(): Observable<any> {
    return this.http.get(`${this.apiUrl}/settings`);
  }

  updateServerSettings(settings: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/settings`, settings);
  }

  // Races
  getAllRaces(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/races`);
  }

  createRace(race: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/races`, race);
  }

  updateRace(id: number, race: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/races/${id}`, race);
  }

  deleteRace(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/races/${id}`);
  }

  // Character Classes
  getAllCharacterClasses(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/classes`);
  }

  createCharacterClass(characterClass: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/classes`, characterClass);
  }

  updateCharacterClass(id: number, characterClass: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/classes/${id}`, characterClass);
  }

  deleteCharacterClass(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/classes/${id}`);
  }

  // Skills Registry
  getAllSkills(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/skills`);
  }
}
