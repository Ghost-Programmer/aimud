import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, Subject} from 'rxjs';
import {tap} from 'rxjs/operators';

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
  public storesUpdated$ = new Subject<void>();

  constructor(private http: HttpClient) {}

  getAllStores(): Observable<Store[]> {
    return this.http.get<Store[]>(this.apiUrl);
  }

  getStore(id: number): Observable<Store> {
    return this.http.get<Store>(`${this.apiUrl}/${id}`);
  }

  createStore(store: Store): Observable<Store> {
    return this.http.post<Store>(this.apiUrl, store).pipe(
      tap(() => this.storesUpdated$.next())
    );
  }

  updateStore(id: number, store: Store): Observable<Store> {
    return this.http.put<Store>(`${this.apiUrl}/${id}`, store).pipe(
      tap(() => this.storesUpdated$.next())
    );
  }

  deleteStore(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.storesUpdated$.next())
    );
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

  getStoreDialog(storeId: number, characterId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${storeId}/dialog?characterId=${characterId}`);
  }

  buyItem(storeId: number, itemId: number, characterId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${storeId}/buy/${itemId}?characterId=${characterId}`, {});
  }

  sellItem(storeId: number, itemId: number, characterId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${storeId}/sell/${itemId}?characterId=${characterId}`, {});
  }
}
