import {Injectable} from '@angular/core';
import {webSocket, WebSocketSubject} from 'rxjs/webSocket';
import {filter, Observable, retry, shareReplay, tap} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GameWebSocketService {
  private socket$: WebSocketSubject<any>;
  private messages$: Observable<any>;

  constructor() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    const wsUrl = `${protocol}//${host}/ws/game`;

    this.socket$ = webSocket({
      url: wsUrl,
      openObserver: {
        next: () => console.log('WebSocket connected')
      },
      closeObserver: {
        next: () => console.log('WebSocket disconnected')
      }
    });

    this.messages$ = this.socket$.pipe(
      retry({delay: 3000}),
      shareReplay({bufferSize: 10, refCount: true})
    );
  }

  public getCharacterUpdates(characterId: number): Observable<any> {
    return this.messages$.pipe(
      tap((msg: any) => console.log('WS Filter checking msg:', msg)),
      filter((msg: any) => {
        if (!msg) return false;
        // Match targeted messages or broadcast messages (id: -1)
        // Use string conversion to handle potential type differences (string vs number)
        return String(msg.id) === String(characterId) || String(msg.id) === '-1';
      })
    );
  }

  public getAllMessages(): Observable<any> {
    return this.messages$.pipe(
      tap((msg: any) => console.log('WS Raw msg:', msg))
    );
  }
}
