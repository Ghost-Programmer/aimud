import { Injectable } from '@angular/core';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { Observable, filter, retry } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GameWebSocketService {
  private socket$: WebSocketSubject<any>;

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
  }

  public getCharacterUpdates(characterId: number): Observable<any> {
    return this.socket$.pipe(
      retry({ delay: 3000 }),
      filter(msg => {
        if (!msg) return false;
        // Match targeted messages or broadcast messages (id: -1)
        return msg.id === characterId || msg.id === -1;
      })
    );
  }
}
