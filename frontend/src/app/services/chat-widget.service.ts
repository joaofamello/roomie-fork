import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type WidgetState = 'closed' | 'list' | 'chat';

@Injectable({ providedIn: 'root' })
export class ChatWidgetService {
  private readonly _state  = new BehaviorSubject<WidgetState>('closed');
  private readonly _chatId = new BehaviorSubject<number | null>(null);

  readonly state$  = this._state.asObservable();
  readonly chatId$ = this._chatId.asObservable();

  toggle(): void {
    this._state.value === 'closed' ? this.openList() : this.close();
  }

  openList(): void {
    this._state.next('list');
  }

  openChat(chatId: number): void {
    this._chatId.next(chatId);
    this._state.next('chat');
  }

  backToList(): void {
    this._chatId.next(null);
    this._state.next('list');
  }

  close(): void {
    this._chatId.next(null);
    this._state.next('closed');
  }
}

