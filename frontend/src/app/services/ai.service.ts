import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AiService {
  private apiUrl = '/api/ai';

  constructor(private http: HttpClient) {}

  processPrompt(prompt: string): Observable<string> {
    return new Observable<string>(observer => {
      fetch(`${this.apiUrl}/prompt`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'text/event-stream'
        },
        body: JSON.stringify({ prompt })
      }).then(response => {
        const reader = response.body?.getReader();
        if (!reader) {
          observer.error('No response body');
          return;
        }

        const decoder = new TextDecoder();
        const read = () => {
          reader.read().then(({ done, value }) => {
            if (done) {
              observer.complete();
              return;
            }
            const chunk = decoder.decode(value, { stream: true });
            // SSE chunks usually look like "data: ...\n\n"
            const lines = chunk.split('\n');
            for (const line of lines) {
              if (line.startsWith('data:')) {
                let data = line.slice(5);
                if (data.endsWith('\r')) {
                  data = data.slice(0, -1);
                }
                if (data) {
                  observer.next(data);
                }
              }
            }
            read();
          }).catch(error => {
            observer.error(error);
          });
        }
        read();
      }).catch(error => {
        observer.error(error);
      });
    });
  }
}
