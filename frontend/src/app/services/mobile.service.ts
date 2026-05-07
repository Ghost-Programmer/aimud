import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Mobile} from '../models/mobile.model';

@Injectable({
  providedIn: 'root'
})
export class MobileService {
  private apiUrl = '/api/mobiles';

  constructor(private http: HttpClient) {
  }

  getAllMobiles(page: number = 0, size: number = 10, name: string = ''): Observable<any> {
    const params: any = { page, size };
    if (name) params.name = name;
    return this.http.get<any>(this.apiUrl, { params });
  }

  getMobile(id: number): Observable<Mobile> {
    return this.http.get<Mobile>(`${this.apiUrl}/${id}`);
  }

  createMobile(mobile: Mobile): Observable<Mobile> {
    return this.http.post<Mobile>(this.apiUrl, mobile);
  }

  updateMobile(id: number, mobile: Mobile): Observable<Mobile> {
    return this.http.put<Mobile>(`${this.apiUrl}/${id}`, mobile);
  }

  deleteMobile(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getMobileActions(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${id}/actions`);
  }

  saveMobileActions(id: number, actions: any[]): Observable<any[]> {
    return this.http.post<any[]>(`${this.apiUrl}/${id}/actions`, actions);
  }
}
