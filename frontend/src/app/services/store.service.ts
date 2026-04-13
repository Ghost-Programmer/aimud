import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

export interface Store {
  id?: number;
  name: string;
  description: string;
}

@Injectable({
  providedIn: 'root'
})
export class StoreService {
  private apiUrl = '/api/stores';

  constructor(private http: HttpClient) {}

  getAllStores(): Observable<Store[]> {
    return this.http.get<Store[]>(this.apiUrl);
  }

  getStore(id: number): Observable<Store> {
    return this.http.get<Store>(`${this.apiUrl}/${id}`);
  }

  createStore(store: Store): Observable<Store> {
    return this.http.post<Store>(this.apiUrl, store);
  }

  updateStore(id: number, store: Store): Observable<Store> {
    return this.http.put<Store>(`${this.apiUrl}/${id}`, store);
  }

  deleteStore(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getStoreItems(storeId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${storeId}/items`);
  }

  addStoreItem(storeId: number, itemId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${storeId}/items/${itemId}`, {});
  }

  removeStoreItem(storeId: number, itemId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${storeId}/items/${itemId}`);
  }
}
