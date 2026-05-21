import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PagedRooms, Room, RoomType} from '../models/room.model';

@Injectable({
  providedIn: 'root'
})
export class RoomService {
  private apiUrl = '/api/rooms';

  constructor(private http: HttpClient) {
  }

  getRooms(page: number, size: number, filters: {
    name?: string,
    type?: RoomType,
    minId?: number,
    maxId?: number,
    roomHouse?: boolean
  }): Observable<PagedRooms> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (filters.name) params = params.set('name', filters.name);
    if (filters.type) params = params.set('type', filters.type);
    if (filters.minId !== undefined) params = params.set('minId', filters.minId.toString());
    if (filters.maxId !== undefined) params = params.set('maxId', filters.maxId.toString());
    if (filters.roomHouse !== undefined) params = params.set('roomHouse', filters.roomHouse.toString());

    return this.http.get<PagedRooms>(this.apiUrl, {params});
  }

  getRoom(id: number): Observable<Room> {
    return this.http.get<Room>(`${this.apiUrl}/${id}`);
  }

  createRoom(room: Room): Observable<Room> {
    return this.http.post<Room>(this.apiUrl, room);
  }

  updateRoom(id: number, room: Room): Observable<Room> {
    return this.http.put<Room>(`${this.apiUrl}/${id}`, room);
  }

  deleteRoom(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
