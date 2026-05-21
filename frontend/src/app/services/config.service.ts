import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {shareReplay} from 'rxjs/operators';
import {Agent} from '../models/agent.model';
import {ItemTypeDetails} from '../models/item.model';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  private apiUrl = '/api/config';
  private itemTypesDetails$: Observable<{[key: string]: ItemTypeDetails}> | null = null;

  constructor(private http: HttpClient) {
  }

  // Server Settings
  getServerSettings(): Observable<any> {
    return this.http.get(`${this.apiUrl}/settings`);
  }

  getItemTypes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/item-types`);
  }

  getItemTypesDetails(): Observable<{[key: string]: ItemTypeDetails}> {
    if (!this.itemTypesDetails$) {
      this.itemTypesDetails$ = this.http.get<{[key: string]: ItemTypeDetails}>(`${this.apiUrl}/item-types/details`).pipe(
        shareReplay(1)
      );
    }
    return this.itemTypesDetails$;
  }

  getWearLocations(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/wear-locations`);
  }

  updateServerSettings(settings: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/settings`, settings);
  }

  // Agents
  getAllAgents(page: number = 0, size: number = 10): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/agents`, { params: { page, size } });
  }

  createAgent(agent: Agent): Observable<Agent> {
    return this.http.post<Agent>(`${this.apiUrl}/agents`, agent);
  }

  updateAgent(id: number, agent: Agent): Observable<Agent> {
    return this.http.put<Agent>(`${this.apiUrl}/agents/${id}`, agent);
  }

  deleteAgent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/agents/${id}`);
  }

  // Races
  getAllRaces(page: number = 0, size: number = 10, search: string = ''): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/races`, { params: { page, size, search } });
  }

  getPlayableRaces(page: number = 0, size: number = 10, search: string = ''): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/races`, {params: {playableOnly: 'true', page, size, search}});
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
  getAllCharacterClasses(page: number = 0, size: number = 10, search: string = ''): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/classes`, { params: { page, size, search } });
  }

  getPlayableCharacterClasses(page: number = 0, size: number = 10, search: string = ''): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/classes`, {params: {playableOnly: 'true', page, size, search}});
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

  // Factions
  getAllFactions(): Observable<any[]> {
    return this.http.get<any[]>('/api/factions');
  }

  createFaction(faction: any): Observable<any> {
    return this.http.post('/api/factions', faction);
  }

  updateFaction(id: number, faction: any): Observable<any> {
    return this.http.put(`/api/factions/${id}`, faction);
  }

  deleteFaction(id: number): Observable<any> {
    return this.http.delete(`/api/factions/${id}`);
  }
}
