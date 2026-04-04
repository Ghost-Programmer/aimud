import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AiService {
  private apiUrl = '/api/ai';

  constructor(private http: HttpClient) {
  }

  processPrompt(prompt: string): Observable<{ response: string }> {
    return this.http.post<{ response: string }>(`${this.apiUrl}/prompt`, {prompt});
  }
}
