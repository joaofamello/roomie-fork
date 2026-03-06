import {
  Component, OnInit, OnDestroy, AfterViewChecked,
  ViewChild, ElementRef, ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, NavigationEnd } from '@angular/router';
import { Subscription, filter, take } from 'rxjs';
import { ChatService } from '../../services/chat.service';
import { ChatWidgetService, WidgetState } from '../../services/chat-widget.service';
import { Auth } from '../../auth/auth';
import { Chat } from '../../models/chat.model';
import { Message } from '../../models/message.model';

@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-widget.component.html',
  styleUrl: './chat-widget.component.css'
})
export class ChatWidgetComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('msgContainer') msgContainer?: ElementRef;

  state: WidgetState = 'closed';
  chats: Chat[] = [];
  messages: Message[] = [];
  activeChat: Chat | null = null;
  newMessage = '';
  isSending = false;
  isLoadingChats = false;
  isLoadingMessages = false;
  currentUserId: number | null = null;
  isAuthenticated = false;
  isOnChatPage = false;

  private subs = new Subscription();
  private pollInterval: ReturnType<typeof setInterval> | null = null;
  private chatPollInterval: ReturnType<typeof setInterval> | null = null;
  private shouldScroll = false;

  /** Total de mensagens não lidas em todos os chats */
  get totalUnread(): number {
    return this.chats.reduce((sum, c) => sum + (c.unreadCount ?? 0), 0);
  }

  /** FAB só aparece quando autenticado, há pelo menos 1 chat e NÃO está na página de mensagens */
  get showFab(): boolean {
    return this.isAuthenticated && this.chats.length > 0 && !this.isOnChatPage;
  }

  constructor(
    private readonly chatService: ChatService,
    private readonly widgetService: ChatWidgetService,
    private readonly auth: Auth,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // Detecta se está na página de chats para esconder o widget
    this.isOnChatPage = this.router.url.startsWith('/chats');
    this.subs.add(
      this.router.events.pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd)
      ).subscribe(e => {
        this.isOnChatPage = e.urlAfterRedirects.startsWith('/chats');
        if (this.isOnChatPage) {
          this.widgetService.close();
        }
        this.cdr.detectChanges();
      })
    );

    // Acompanha autenticação — mostra/oculta widget dinamicamente
    this.subs.add(
      this.auth.currentUser$.subscribe(user => {
        this.isAuthenticated = !!user;
        this.currentUserId = user?.id ?? null;

        if (this.isAuthenticated) {
          this.refreshChatList();
          if (!this.chatPollInterval) {
            this.chatPollInterval = setInterval(() => this.refreshChatList(), 30000);
          }
        } else {
          // Usuário deslogou: limpa tudo
          this.chats = [];
          this.messages = [];
          this.activeChat = null;
          this.widgetService.close();
          this.stopChatListPolling();
        }
        this.cdr.detectChanges();
      })
    );

    this.subs.add(
      this.widgetService.state$.subscribe(state => {
        this.state = state;
        if (state === 'list') this.loadChats();
        this.cdr.detectChanges();
      })
    );

    this.subs.add(
      this.widgetService.chatId$.subscribe(id => {
        if (id !== null) this.loadChat(id);
      })
    );
  }

  /** Atualiza a lista de chats silenciosamente (sem spinner) — usado para o badge */
  private refreshChatList(): void {
    this.chatService.getMyChats().subscribe({
      next: chats => {
        this.chats = chats.sort((a, b) =>
          new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
        );
        this.cdr.detectChanges();
      }
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.stopPolling();
    this.stopChatListPolling();
  }

  private stopChatListPolling(): void {
    if (this.chatPollInterval) {
      clearInterval(this.chatPollInterval);
      this.chatPollInterval = null;
    }
  }

  // ── list ───

  loadChats(): void {
    this.isLoadingChats = true;
    this.chatService.getMyChats().subscribe({
      next: chats => {
        this.chats = chats.sort((a, b) =>
          new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
        );
        this.isLoadingChats = false;
        this.cdr.detectChanges();
      },
      error: () => { this.isLoadingChats = false; this.cdr.detectChanges(); }
    });
  }

  selectChat(chat: Chat): void {
    this.widgetService.openChat(chat.id);
  }

  otherName(chat: Chat): string {
    return this.currentUserId === chat.ownerId ? chat.studentName : chat.ownerName;
  }

  // ── chat ───
  loadChat(chatId: number): void {
    this.isLoadingMessages = true;
    this.activeChat = null;
    this.messages = [];
    this.stopPolling();

    this.chatService.getChatById(chatId).subscribe({
      next: chat => {
        this.activeChat = chat;
        this.fetchMessages(chatId);
        this.startPolling(chatId);
        this.cdr.detectChanges();
      },
      error: () => { this.isLoadingMessages = false; this.cdr.detectChanges(); }
    });
  }

  fetchMessages(chatId: number): void {
    this.chatService.getMessages(chatId).subscribe({
      next: msgs => {
        this.messages = msgs;
        this.isLoadingMessages = false;
        this.shouldScroll = true;
        this.cdr.detectChanges();
      },
      error: () => { this.isLoadingMessages = false; this.cdr.detectChanges(); }
    });
  }

  startPolling(chatId: number): void {
    this.pollInterval = setInterval(() => {
      this.chatService.getMessages(chatId).subscribe({
        next: msgs => {
          if (msgs.length !== this.messages.length) {
            this.messages = msgs;
            this.shouldScroll = true;
            this.cdr.detectChanges();
          }
        }
      });
    }, 5000);
  }

  stopPolling(): void {
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
      this.pollInterval = null;
    }
  }

  sendMessage(): void {
    const content = this.newMessage.trim();
    if (!content || !this.activeChat || this.isSending) return;
    this.isSending = true;
    this.chatService.sendMessage(this.activeChat.id, content).subscribe({
      next: msg => {
        this.messages.push(msg);
        this.newMessage = '';
        this.isSending = false;
        this.shouldScroll = true;
        this.cdr.detectChanges();
      },
      error: () => { this.isSending = false; }
    });
  }

  onKeydown(e: KeyboardEvent): void {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); this.sendMessage(); }
  }

  isMine(msg: Message): boolean {
    return msg.senderId === this.currentUserId;
  }

  // ── navigation ───

  toggle(): void   { this.widgetService.toggle(); }
  backToList(): void { this.stopPolling(); this.widgetService.backToList(); }
  close(): void    { this.stopPolling(); this.widgetService.close(); }

  navigateToChats(): void {
    this.close();
    this.router.navigate(['/chats']);
  }

  private scrollToBottom(): void {
    try {
      const el = this.msgContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch (_) {
      // Scroll is best-effort; ignore errors if element is not yet rendered
    }
  }
}




