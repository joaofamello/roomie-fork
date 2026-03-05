import { TestBed } from '@angular/core/testing';
import { ChatWidgetService } from './chat-widget.service';

describe('ChatWidgetService', () => {
  let service: ChatWidgetService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [ChatWidgetService] });
    service = TestBed.inject(ChatWidgetService);
  });

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('deve iniciar com estado "closed"', (done) => {
    service.state$.subscribe(state => {
      expect(state).toBe('closed');
      done();
    });
  });

  it('deve iniciar com chatId nulo', (done) => {
    service.chatId$.subscribe(id => {
      expect(id).toBeNull();
      done();
    });
  });

  // ── openList ────

  it('openList() deve mudar o estado para "list"', (done) => {
    service.openList();
    service.state$.subscribe(state => {
      expect(state).toBe('list');
      done();
    });
  });

  // ── openChat ────

  it('openChat(id) deve mudar o estado para "chat" e definir o chatId', () => {
    const states: string[] = [];
    const ids: (number | null)[] = [];

    service.state$.subscribe(s => states.push(s));
    service.chatId$.subscribe(id => ids.push(id));

    service.openChat(42);

    expect(states).toContain('chat');
    expect(ids).toContain(42);
  });

  // ── toggle ─────

  it('toggle() deve abrir a lista quando fechado', (done) => {
    service.toggle();
    service.state$.subscribe(state => {
      expect(state).toBe('list');
      done();
    });
  });

  it('toggle() deve fechar o widget quando aberto', () => {
    const states: string[] = [];
    service.state$.subscribe(s => states.push(s));

    service.openList();  // 'list'
    service.toggle();    // 'closed'

    expect(states.at(-1)).toBe('closed');
  });

  it('toggle() deve fechar quando estiver em modo "chat"', () => {
    const states: string[] = [];
    service.state$.subscribe(s => states.push(s));

    service.openChat(10);  // 'chat'
    service.toggle();      // 'closed'

    expect(states.at(-1)).toBe('closed');
  });

  // ── backToList ────

  it('backToList() deve voltar ao estado "list" e limpar o chatId', () => {
    const states: string[] = [];
    const ids: (number | null)[] = [];

    service.state$.subscribe(s => states.push(s));
    service.chatId$.subscribe(id => ids.push(id));

    service.openChat(10);
    service.backToList();

    expect(states.at(-1)).toBe('list');
    expect(ids.at(-1)).toBeNull();
  });

  // ── close ───

  it('close() deve retornar ao estado "closed" e limpar o chatId', () => {
    const states: string[] = [];
    const ids: (number | null)[] = [];

    service.state$.subscribe(s => states.push(s));
    service.chatId$.subscribe(id => ids.push(id));

    service.openChat(99);
    service.close();

    expect(states.at(-1)).toBe('closed');
    expect(ids.at(-1)).toBeNull();
  });

  it('close() a partir da lista deve fechar tudo', () => {
    const states: string[] = [];
    service.state$.subscribe(s => states.push(s));

    service.openList();
    service.close();

    expect(states.at(-1)).toBe('closed');
  });
});

