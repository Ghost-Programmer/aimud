import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CharacterService {
  private apiUrl = '/api/characters';

  constructor(private http: HttpClient) { }

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

  getAvailableCharacters(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/available`);
  }
}
