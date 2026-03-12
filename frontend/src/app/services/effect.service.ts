import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Effect, PagedEffects } from '../models/effect.model';
import { EffectType } from '../models/item.model';

@Injectable({
  providedIn: 'root'
})
export class EffectService {
  private apiUrl = '/api/effects';

  constructor(private http: HttpClient) {}

  getEffects(page: number, size: number, filters: {
    name?: string,
    type?: EffectType,
    sort?: string
  }): Observable<PagedEffects> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filters.name) params = params.set('name', filters.name);
    if (filters.type) params = params.set('type', filters.type);
    if (filters.sort) params = params.set('sort', filters.sort);

    return this.http.get<PagedEffects>(this.apiUrl, { params });
  }

  getEffect(id: number): Observable<Effect> {
    return this.http.get<Effect>(`${this.apiUrl}/${id}`);
  }

  createEffect(effect: Effect): Observable<Effect> {
    return this.http.post<Effect>(this.apiUrl, effect);
  }

  updateEffect(id: number, effect: Effect): Observable<Effect> {
    return this.http.put<Effect>(`${this.apiUrl}/${id}`, effect);
  }

  deleteEffect(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
