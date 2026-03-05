import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ChatService } from './chat.service';
import { Chat } from '../models/chat.model';
import { Message } from '../models/message.model';
import { environment } from '../../enviroments/enviroment';

describe('ChatService', () => {
  let service: ChatService;
  let httpMock: HttpTestingController;

  const BASE = `${environment.apiUrl}/api/chats`;

  const mockChat: Chat = {
    id: 100,
    studentId: 2,
    studentName: 'Estudante Teste',
    ownerId: 1,
    ownerName: 'Proprietário Teste',
    propertyId: 10,
    propertyTitle: 'Quarto perto da UFAPE',
    timestamp: '2026-03-05T12:00:00Z',
    unreadCount: 3
  };

  const mockMessage: Message = {
    id: 200,
    chatId: 100,
    senderId: 1,
    senderName: 'Proprietário Teste',
    content: 'Olá, tudo bem?',
    timestamp: '2026-03-05T13:00:00Z',
    read: false
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ChatService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(ChatService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  // ── startChat ───

  it('deve iniciar um chat via POST e retornar o ChatResponseDTO', () => {
    service.startChat(2, 10).subscribe(result => {
      expect(result).toEqual(mockChat);
    });

    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ studentId: 2, propertyId: 10 });
    req.flush(mockChat);
  });

  // ── getMyChats ───

  it('deve listar os chats do usuário via GET', () => {
    service.getMyChats().subscribe(result => {
      expect(result).toEqual([mockChat]);
      expect(result.length).toBe(1);
    });

    const req = httpMock.expectOne(BASE);
    expect(req.request.method).toBe('GET');
    req.flush([mockChat]);
  });

  it('deve retornar lista vazia quando não houver chats', () => {
    service.getMyChats().subscribe(result => {
      expect(result).toEqual([]);
    });

    const req = httpMock.expectOne(BASE);
    req.flush([]);
  });

  // ── getChatById ───

  it('deve buscar um chat por ID via GET', () => {
    service.getChatById(100).subscribe(result => {
      expect(result).toEqual(mockChat);
      expect(result.id).toBe(100);
    });

    const req = httpMock.expectOne(`${BASE}/100`);
    expect(req.request.method).toBe('GET');
    req.flush(mockChat);
  });

  // ── getMessages ────

  it('deve buscar mensagens de um chat via GET', () => {
    service.getMessages(100).subscribe(result => {
      expect(result).toEqual([mockMessage]);
      expect(result[0].content).toBe('Olá, tudo bem?');
    });

    const req = httpMock.expectOne(`${BASE}/100/messages`);
    expect(req.request.method).toBe('GET');
    req.flush([mockMessage]);
  });

  it('deve retornar lista vazia quando não houver mensagens', () => {
    service.getMessages(100).subscribe(result => {
      expect(result).toEqual([]);
    });

    const req = httpMock.expectOne(`${BASE}/100/messages`);
    req.flush([]);
  });

  // ── sendMessage ────

  it('deve enviar uma mensagem via POST e retornar o MessageResponseDTO', () => {
    service.sendMessage(100, 'Olá, tudo bem?').subscribe(result => {
      expect(result).toEqual(mockMessage);
      expect(result.content).toBe('Olá, tudo bem?');
      expect(result.read).toBeFalse();
    });

    const req = httpMock.expectOne(`${BASE}/100/messages`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ content: 'Olá, tudo bem?' });
    req.flush(mockMessage);
  });

  it('deve incluir o conteúdo correto no corpo da requisição ao enviar mensagem', () => {
    const content = 'Mensagem de teste';
    service.sendMessage(100, content).subscribe();

    const req = httpMock.expectOne(`${BASE}/100/messages`);
    expect(req.request.body).toEqual({ content });
    req.flush({ ...mockMessage, content });
  });
});

