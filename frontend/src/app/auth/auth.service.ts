import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface AuthResponse {
  token?: string;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly TOKEN_KEY = 'aimud_jwt_token';

  // Reactive state for authentication
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient) { }

  public login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', credentials).pipe(
      tap(response => {
        if (response.token) {
          this.setToken(response.token);
        }
      })
    );
  }

  public register(user: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/register', user);
  }

  public logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.isAuthenticatedSubject.next(false);
  }

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
    this.isAuthenticatedSubject.next(true);
  }

  public getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  public hasToken(): boolean {
    return !!this.getToken();
  }

  // Basic JWT decoding to extract the roles claim we added in JwtService
  public getRoles(): string[] {
    const token = this.getToken();
    if (!token) return [];

    try {
      const payloadBase64 = token.split('.')[1];
      const payloadJson = atob(payloadBase64);
      const payload = JSON.parse(payloadJson);
      if (payload.roles) {
        return payload.roles.split(',').map((r: string) => r.trim());
      }
      return [];
    } catch (e) {
      console.error('Failed to parse JWT payload', e);
      return [];
    }
  }

  public isAdmin(): boolean {
    return this.getRoles().includes('ADMIN');
  }

  public getUsername(): string {
    const token = this.getToken();
    if (!token) return '';

    try {
      const payloadBase64 = token.split('.')[1];
      const payloadJson = atob(payloadBase64);
      const payload = JSON.parse(payloadJson);
      return payload.sub || '';
    } catch (e) {
      return '';
    }
  }
}
