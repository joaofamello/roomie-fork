import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of, BehaviorSubject } from 'rxjs';
import { ChatWidgetComponent } from './chat-widget.component';
import { ChatService } from '../../services/chat.service';
import { ChatWidgetService } from '../../services/chat-widget.service';
import { Auth } from '../../auth/auth';
import { Chat } from '../../models/chat.model';
import { Message } from '../../models/message.model';

const mockChat: Chat = {
  id: 100,
  studentId: 2,
  studentName: 'Estudante Teste',
  ownerId: 1,
  ownerName: 'Proprietário Teste',
  propertyId: 10,
  propertyTitle: 'Quarto perto da UFAPE',
  timestamp: '2026-03-05T12:00:00Z',
  unreadCount: 2
};

const mockMessage: Message = {
  id: 200,
  chatId: 100,
  senderId: 1,
  senderName: 'Proprietário Teste',
  content: 'Olá!',
  timestamp: '2026-03-05T13:00:00Z',
  read: false
};

describe('ChatWidgetComponent', () => {
  let fixture: ComponentFixture<ChatWidgetComponent>;
  let component: ChatWidgetComponent;

  const stateSubject  = new BehaviorSubject<any>('closed');
  const chatIdSubject = new BehaviorSubject<number | null>(null);
  const userSubject   = new BehaviorSubject<any>(null);

  const mockChatService = {
    getMyChats:  jest.fn().mockReturnValue(of([])),
    getChatById: jest.fn().mockReturnValue(of(mockChat)),
    getMessages: jest.fn().mockReturnValue(of([])),
    sendMessage: jest.fn().mockReturnValue(of(mockMessage))
  };

  const mockWidgetService = {
    state$:     stateSubject.asObservable(),
    chatId$:    chatIdSubject.asObservable(),
    toggle:     jest.fn(),
    openList:   jest.fn(),
    openChat:   jest.fn(),
    backToList: jest.fn(),
    close:      jest.fn()
  };

  const mockAuth = {
    currentUser$: userSubject.asObservable()
  };

  beforeEach(async () => {
    jest.clearAllMocks();
    mockChatService.getMyChats.mockReturnValue(of([]));
    mockChatService.getChatById.mockReturnValue(of(mockChat));
    mockChatService.getMessages.mockReturnValue(of([]));
    mockChatService.sendMessage.mockReturnValue(of(mockMessage));

    await TestBed.configureTestingModule({
      imports: [ChatWidgetComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ChatService,       useValue: mockChatService },
        { provide: ChatWidgetService, useValue: mockWidgetService },
        { provide: Auth,              useValue: mockAuth }
      ]
    }).compileComponents();

    fixture   = TestBed.createComponent(ChatWidgetComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    stateSubject.next('closed');
    chatIdSubject.next(null);
    userSubject.next(null);
  });

  it('deve ser criado', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  // ── Visibilidade por autenticação ─────────────────────────────────────────

  it('não deve mostrar o FAB quando usuário não está autenticado', () => {
    userSubject.next(null);
    fixture.detectChanges();
    expect(component.isAuthenticated).toBe(false);
    const fab = fixture.nativeElement.querySelector('.fab');
    expect(fab).toBeNull();
  });

  it('não deve mostrar o FAB quando autenticado mas sem chats', () => {
    userSubject.next({ id: 1, name: 'Dono', email: 'dono@roomie.com', role: 'USER' });
    component.chats = [];
    fixture.detectChanges();
    expect(component.showFab).toBe(false);
  });

  it('deve mostrar o FAB quando autenticado e com chats e estado closed', () => {
    mockChatService.getMyChats.mockReturnValue(of([mockChat]));
    userSubject.next({ id: 1, name: 'Dono', email: 'dono@roomie.com', role: 'USER' });
    stateSubject.next('closed');
    fixture.detectChanges();
    expect(component.showFab).toBe(true);
    const fab = fixture.nativeElement.querySelector('.fab');
    expect(fab).not.toBeNull();
  });

  // ── Badge de não lidas ────────────────────────────────────────────────────

  it('totalUnread deve somar os unreadCount de todos os chats', () => {
    component.chats = [
      { ...mockChat, unreadCount: 2 },
      { ...mockChat, id: 101, unreadCount: 5 }
    ];
    expect(component.totalUnread).toBe(7);
  });

  it('totalUnread deve ser 0 quando não há chats', () => {
    component.chats = [];
    expect(component.totalUnread).toBe(0);
  });

  it('deve exibir o badge quando há mensagens não lidas', () => {
    mockChatService.getMyChats.mockReturnValue(of([{ ...mockChat, unreadCount: 3 }]));
    userSubject.next({ id: 1, name: 'Dono', email: 'dono@roomie.com', role: 'USER' });
    stateSubject.next('closed');
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.fab-badge');
    expect(badge).not.toBeNull();
    expect(badge.textContent.trim()).toBe('3');
  });

  it('não deve exibir o badge quando não há mensagens não lidas', () => {
    mockChatService.getMyChats.mockReturnValue(of([{ ...mockChat, unreadCount: 0 }]));
    userSubject.next({ id: 1, name: 'Dono', email: 'dono@roomie.com', role: 'USER' });
    stateSubject.next('closed');
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.fab-badge');
    expect(badge).toBeNull();
  });

  it('deve mostrar "99+" quando há mais de 99 não lidas', () => {
    mockChatService.getMyChats.mockReturnValue(of([{ ...mockChat, unreadCount: 150 }]));
    userSubject.next({ id: 1, name: 'Dono', email: 'dono@roomie.com', role: 'USER' });
    stateSubject.next('closed');
    fixture.detectChanges();
    const badge = fixture.nativeElement.querySelector('.fab-badge');
    expect(badge?.textContent?.trim()).toBe('99+');
  });

  // ── Painel ────────────────────────────────────────────────────────────────

  it('não deve mostrar o painel quando estado é "closed"', () => {
    userSubject.next({ id: 1, name: 'Dono', email: 'dono@roomie.com', role: 'USER' });
    stateSubject.next('closed');
    fixture.detectChanges();
    const panel = fixture.nativeElement.querySelector('.widget-panel');
    expect(panel).toBeNull();
  });

  it('deve mostrar o painel quando estado é "list"', () => {
    userSubject.next({ id: 1, name: 'Dono', email: 'dono@roomie.com', role: 'USER' });
    mockChatService.getMyChats.mockReturnValue(of([mockChat]));
    stateSubject.next('list');
    fixture.detectChanges();
    const panel = fixture.nativeElement.querySelector('.widget-panel');
    expect(panel).not.toBeNull();
  });

  // ── otherName ─────────────────────────────────────────────────────────────

  it('otherName() deve retornar o nome do estudante quando o usuário é o dono', () => {
    component.currentUserId = 1;
    expect(component.otherName(mockChat)).toBe('Estudante Teste');
  });

  it('otherName() deve retornar o nome do dono quando o usuário é o estudante', () => {
    component.currentUserId = 2;
    expect(component.otherName(mockChat)).toBe('Proprietário Teste');
  });

  // ── isMine ────────────────────────────────────────────────────────────────

  it('isMine() deve retornar true quando a mensagem é do usuário atual', () => {
    component.currentUserId = 1;
    expect(component.isMine(mockMessage)).toBe(true);
  });

  it('isMine() deve retornar false quando a mensagem é de outro usuário', () => {
    component.currentUserId = 2;
    expect(component.isMine(mockMessage)).toBe(false);
  });

  // ── Navegação ─────────────────────────────────────────────────────────────

  it('toggle() deve delegar ao ChatWidgetService', () => {
    component.toggle();
    expect(mockWidgetService.toggle).toHaveBeenCalled();
  });

  it('backToList() deve delegar ao ChatWidgetService', () => {
    component.backToList();
    expect(mockWidgetService.backToList).toHaveBeenCalled();
  });

  it('close() deve delegar ao ChatWidgetService', () => {
    component.close();
    expect(mockWidgetService.close).toHaveBeenCalled();
  });

  it('selectChat() deve chamar openChat com o id do chat', () => {
    component.selectChat(mockChat);
    expect(mockWidgetService.openChat).toHaveBeenCalledWith(100);
  });

  // ── sendMessage ───────────────────────────────────────────────────────────

  it('sendMessage() não deve enviar quando conteúdo está vazio', () => {
    component.newMessage = '   ';
    component.activeChat = mockChat;
    component.sendMessage();
    expect(mockChatService.sendMessage).not.toHaveBeenCalled();
  });

  it('sendMessage() não deve enviar quando não há chat ativo', () => {
    component.newMessage = 'Olá!';
    component.activeChat = null;
    component.sendMessage();
    expect(mockChatService.sendMessage).not.toHaveBeenCalled();
  });

  it('sendMessage() deve enviar a mensagem e limpar o input', () => {
    mockChatService.sendMessage.mockReturnValue(of(mockMessage));
    component.newMessage = 'Olá!';
    component.activeChat = mockChat;
    component.sendMessage();
    expect(mockChatService.sendMessage).toHaveBeenCalledWith(100, 'Olá!');
    expect(component.newMessage).toBe('');
    expect(component.messages).toContain(mockMessage);
  });

  // ── onKeydown ─────────────────────────────────────────────────────────────

  it('onKeydown() deve enviar mensagem ao pressionar Enter sem Shift', () => {
    const sendSpy = jest.spyOn(component, 'sendMessage').mockImplementation(() => {});
    const event = new KeyboardEvent('keydown', { key: 'Enter', shiftKey: false });
    component.onKeydown(event);
    expect(sendSpy).toHaveBeenCalled();
  });

  it('onKeydown() não deve enviar mensagem ao pressionar Shift+Enter', () => {
    const sendSpy = jest.spyOn(component, 'sendMessage').mockImplementation(() => {});
    const event = new KeyboardEvent('keydown', { key: 'Enter', shiftKey: true });
    component.onKeydown(event);
    expect(sendSpy).not.toHaveBeenCalled();
  });
});



