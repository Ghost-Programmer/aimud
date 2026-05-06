import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Agent} from '../models/agent.model';

@Injectable({
  providedIn: 'root'
})
export class ConfigService {
  private apiUrl = '/api/config';

  constructor(private http: HttpClient) {
  }

  // Server Settings
  getServerSettings(): Observable<any> {
    return this.http.get(`${this.apiUrl}/settings`);
  }

  updateServerSettings(settings: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/settings`, settings);
  }

  // Agents
  getAllAgents(): Observable<Agent[]> {
    return this.http.get<Agent[]>(`${this.apiUrl}/agents`);
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
  getAllRaces(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/races`);
  }

  getPlayableRaces(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/races`, {params: {playableOnly: 'true'}});
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

  getPlayableCharacterClasses(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/classes`, {params: {playableOnly: 'true'}});
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
