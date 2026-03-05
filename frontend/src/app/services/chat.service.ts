import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroments/enviroment';
import { Chat } from '../models/chat.model';
import { Message } from '../models/message.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private readonly apiUrl = `${environment.apiUrl}/api/chats`;

  constructor(private readonly http: HttpClient) {}

  /** Inicia um chat (apenas proprietário) */
  startChat(studentId: number, propertyId: number): Observable<Chat> {
    return this.http.post<Chat>(this.apiUrl, { studentId, propertyId });
  }

  /** Lista todos os chats do usuário logado */
  getMyChats(): Observable<Chat[]> {
    return this.http.get<Chat[]>(this.apiUrl);
  }

  /** Retorna detalhes de um chat */
  getChatById(chatId: number): Observable<Chat> {
    return this.http.get<Chat>(`${this.apiUrl}/${chatId}`);
  }

  /** Busca mensagens de um chat (e marca como lidas) */
  getMessages(chatId: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}/${chatId}/messages`);
  }

  /** Envia uma mensagem */
  sendMessage(chatId: number, content: string): Observable<Message> {
    return this.http.post<Message>(`${this.apiUrl}/${chatId}/messages`, { content });
  }
}

