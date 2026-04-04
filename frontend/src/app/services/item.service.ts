import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Item, ItemType, PagedItems, WearLocation} from '../models/item.model';

@Injectable({
  providedIn: 'root'
})
export class ItemService {
  private apiUrl = '/api/items';

  constructor(private http: HttpClient) {
  }

  getItems(page: number, size: number, filters: {
    name?: string,
    type?: ItemType,
    location?: WearLocation,
    minValue?: number,
    maxValue?: number
  }): Observable<PagedItems> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filters.name) params = params.set('name', filters.name);
    if (filters.type) params = params.set('type', filters.type);
    if (filters.location) params = params.set('location', filters.location);
    if (filters.minValue !== undefined) params = params.set('minValue', filters.minValue.toString());
    if (filters.maxValue !== undefined) params = params.set('maxValue', filters.maxValue.toString());

    return this.http.get<PagedItems>(this.apiUrl, {params});
  }

  getItem(id: number): Observable<Item> {
    return this.http.get<Item>(`${this.apiUrl}/${id}`);
  }

  createItem(item: Item): Observable<Item> {
    return this.http.post<Item>(this.apiUrl, item);
  }

  updateItem(id: number, item: Item): Observable<Item> {
    return this.http.put<Item>(`${this.apiUrl}/${id}`, item);
  }

  deleteItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
