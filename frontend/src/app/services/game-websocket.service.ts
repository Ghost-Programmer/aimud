import {Injectable} from '@angular/core';
import {webSocket, WebSocketSubject} from 'rxjs/webSocket';
import {filter, map, Observable, retry, shareReplay, tap} from 'rxjs';
import * as CBOR from 'cbor-web';

@Injectable({
  providedIn: 'root'
})
export class GameWebSocketService {
  private socket$: WebSocketSubject<any>;
  private messages$: Observable<any>;
  private combatSocket$: WebSocketSubject<any>;
  private combatLogs$: Observable<any>;

  constructor() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    const wsUrl = `${protocol}//${host}/ws/game`;
    const combatWsUrl = `${protocol}//${host}/ws/combat_log`;

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

    this.combatSocket$ = webSocket({
      url: combatWsUrl,
      binaryType: 'arraybuffer',
      deserializer: (msg) => msg.data,
      openObserver: {
        next: () => console.log('Combat WebSocket connected')
      },
      closeObserver: {
        next: () => console.log('Combat WebSocket disconnected')
      }
    });

    this.combatLogs$ = this.combatSocket$.pipe(
      retry({delay: 3000}),
      map((data: ArrayBuffer) => CBOR.decode(new Uint8Array(data))),
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

  public getCombatLogs(): Observable<any> {
    return this.combatLogs$;
  }

  public getAllMessages(): Observable<any> {
    return this.messages$.pipe(
      tap((msg: any) => console.log('WS Raw msg:', msg))
    );
  }
}
