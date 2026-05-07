import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Quest {
  id?: number;
  name: string;
  description: string;
  rewardItemId?: number;
  level: number;
}

export interface QuestStep {
  id?: number;
  questId?: number;
  stepNumber: number;
  objectiveType: string;
  targetMobileId?: number;
  targetItemId?: number;
  targetFactionId?: number;
  targetRaceId?: number;
  targetCount: number;
  instructions: string;
}

export interface QuestDrop {
  id?: number;
  questId?: number;
  itemId: number;
  targetMobileId?: number;
  targetFactionId?: number;
  targetRaceId?: number;
  dropChance: number;
}

@Injectable({
  providedIn: 'root'
})
export class QuestService {
  private apiUrl = '/api/quests';

  constructor(private http: HttpClient) { }

  getQuests(): Observable<Quest[]> {
    return this.http.get<Quest[]>(this.apiUrl);
  }

  getQuest(id: number): Observable<Quest> {
    return this.http.get<Quest>(`${this.apiUrl}/${id}`);
  }

  createQuest(quest: Quest): Observable<Quest> {
    return this.http.post<Quest>(this.apiUrl, quest);
  }

  updateQuest(id: number, quest: Quest): Observable<Quest> {
    return this.http.put<Quest>(`${this.apiUrl}/${id}`, quest);
  }

  deleteQuest(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getQuestSteps(questId: number): Observable<QuestStep[]> {
    return this.http.get<QuestStep[]>(`${this.apiUrl}/${questId}/steps`);
  }

  createQuestStep(questId: number, step: QuestStep): Observable<QuestStep> {
    return this.http.post<QuestStep>(`${this.apiUrl}/${questId}/steps`, step);
  }

  deleteQuestStep(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/steps/${id}`);
  }

  getQuestDrops(questId: number): Observable<QuestDrop[]> {
    return this.http.get<QuestDrop[]>(`${this.apiUrl}/${questId}/drops`);
  }

  createQuestDrop(questId: number, drop: QuestDrop): Observable<QuestDrop> {
    return this.http.post<QuestDrop>(`${this.apiUrl}/${questId}/drops`, drop);
  }

  deleteQuestDrop(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/drops/${id}`);
  }
}
