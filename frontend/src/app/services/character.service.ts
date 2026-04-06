import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {MobileMacro} from '../models/mobile-macro.model';

@Injectable({
  providedIn: 'root'
})
export class CharacterService {
  private apiUrl = '/api/characters';

  constructor(private http: HttpClient) {
  }

  createCharacter(character: any): Observable<any> {
    return this.http.post(this.apiUrl, character);
  }

  getCharacters(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  getCharacter(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  getCharacterRoom(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}/room`);
  }

  updateCharacter(id: number, character: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, character);
  }

  generateCharacter(character: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/generate`, character);
  }

  selectCharacter(id: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/select`, {});
  }

  sendCommand(id: number, command: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${id}/command`, command);
  }

  equipItem(characterId: number, itemId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${characterId}/equip/${itemId}`, {});
  }

  unequipItem(characterId: number, slot: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${characterId}/unequip/${slot}`, {});
  }

  dropItem(characterId: number, itemId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${characterId}/drop/${itemId}`, {});
  }

  getAvailableCharacters(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/available`);
  }

  getMacros(characterId: number): Observable<MobileMacro[]> {
    return this.http.get<MobileMacro[]>(`${this.apiUrl}/${characterId}/macros`);
  }

  saveMacros(characterId: number, macros: MobileMacro[]): Observable<MobileMacro[]> {
    return this.http.post<MobileMacro[]>(`${this.apiUrl}/${characterId}/macros`, macros);
  }
}
