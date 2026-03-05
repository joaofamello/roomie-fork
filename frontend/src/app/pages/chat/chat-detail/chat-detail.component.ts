import {
  Component,
  OnInit,
  OnDestroy,
  ViewChild,
  ElementRef,
  AfterViewChecked,
  ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { ChatService } from '../../../services/chat.service';
import { Auth } from '../../../auth/auth';
import { Chat } from '../../../models/chat.model';
import { Message } from '../../../models/message.model';
import { HeaderComponent } from '../../../components/shared/header/header.component';
import { ToastService } from '../../../services/toast.service';
import { take } from 'rxjs';

@Component({
  selector: 'app-chat-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, HeaderComponent],
  templateUrl: './chat-detail.component.html',
  styleUrl: './chat-detail.component.css'
})
export class ChatDetailComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  chat: Chat | null = null;
  messages: Message[] = [];
  newMessage = '';
  isSending = false;
  isLoading = true;
  currentUserId: number | null = null;
  chatId!: number;

  private pollSub: Subscription | null = null;
  private shouldScrollToBottom = false;

  constructor(
    private readonly chatService: ChatService,
    private readonly auth: Auth,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly toast: ToastService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.chatId = Number(this.route.snapshot.paramMap.get('id'));

    this.auth.currentUser$.pipe(take(1)).subscribe(user => {
      this.currentUserId = user?.id ?? null;
      this.loadChat();
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  loadChat(): void {
    this.chatService.getChatById(this.chatId).subscribe({
      next: (chat) => {
        this.chat = chat;
        this.loadMessages();
        this.startPolling();
      },
      error: (err) => {
        console.error('Erro ao carregar chat', err);
        this.toast.error('Erro ao carregar conversa.');
        this.router.navigate(['/chats']);
      }
    });
  }

  loadMessages(): void {
    this.chatService.getMessages(this.chatId).subscribe({
      next: (messages) => {
        this.messages = messages;
        this.isLoading = false;
        this.shouldScrollToBottom = true;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao carregar mensagens', err);
        this.isLoading = false;
      }
    });
  }

  /** Polling a cada 5s para buscar novas mensagens */
  startPolling(): void {
    this.pollSub = interval(5000).pipe(
      switchMap(() => this.chatService.getMessages(this.chatId))
    ).subscribe({
      next: (messages) => {
        const hadNew = messages.length > this.messages.length;
        this.messages = messages;
        if (hadNew) {
          this.shouldScrollToBottom = true;
          this.cdr.detectChanges();
        }
      },
      error: (err) => console.error('Erro no polling', err)
    });
  }

  sendMessage(): void {
    const content = this.newMessage.trim();
    if (!content || this.isSending) return;

    this.isSending = true;
    this.chatService.sendMessage(this.chatId, content).subscribe({
      next: (msg) => {
        this.messages.push(msg);
        this.newMessage = '';
        this.isSending = false;
        this.shouldScrollToBottom = true;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao enviar mensagem', err);
        this.toast.error('Erro ao enviar mensagem.');
        this.isSending = false;
      }
    });
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  isMine(message: Message): boolean {
    return message.senderId === this.currentUserId;
  }

  otherPartyName(): string {
    if (!this.chat) return '';
    return this.currentUserId === this.chat.ownerId
      ? this.chat.studentName
      : this.chat.ownerName;
  }

  private scrollToBottom(): void {
    try {
      const el = this.messagesContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch (_) {}
  }

  goBack(): void {
    this.router.navigate(['/chats']);
  }
}

