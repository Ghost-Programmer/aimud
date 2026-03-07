import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = '/api/users';

  constructor(private http: HttpClient) { }

  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user);
  }

  login(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, user);
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  changePassword(userId: number, password: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${userId}/password`, { password });
  }

  toggleLock(userId: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/${userId}/lock`, {});
  }
}
