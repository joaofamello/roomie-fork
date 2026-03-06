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
import { ContractService } from '../../../services/contract.service';
import { Auth } from '../../../auth/auth';
import { Chat } from '../../../models/chat.model';
import { Message } from '../../../models/message.model';
import { ContractResponse } from '../../../models/contract.model';
import { HeaderComponent } from '../../../components/shared/header/header.component';
import { ContractFormModalComponent } from '../contract-form-modal/contract-form-modal.component';
import { ToastService } from '../../../services/toast.service';
import { take } from 'rxjs';

@Component({
  selector: 'app-chat-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, HeaderComponent, ContractFormModalComponent],
  templateUrl: './chat-detail.component.html',
  styleUrl: './chat-detail.component.css'
})
export class ChatDetailComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  chat: Chat | null = null;
  messages: Message[] = [];
  contracts: ContractResponse[] = [];
  newMessage = '';
  isSending = false;
  isLoading = true;
  isProcessingContract = false;
  showContractModal = false;
  currentUserId: number | null = null;
  chatId!: number;

  private pollSub: Subscription | null = null;
  private shouldScrollToBottom = false;

  constructor(
    private readonly chatService: ChatService,
    private readonly contractService: ContractService,
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
        this.loadContracts();
        this.startPolling();
      },
      error: () => {
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
      error: () => {
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
      error: () => { /* polling errors are non-critical, next tick will retry */ }
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
      error: () => {
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

  isOwner(): boolean {
    return !!this.chat && this.currentUserId === this.chat.ownerId;
  }

  loadContracts(): void {
    this.contractService.getContractsByChat(this.chatId).subscribe({
      next: (contracts) => {
        this.contracts = contracts;
        this.cdr.detectChanges();
      },
      error: () => { /* contracts are optional display info */ }
    });
  }

  acceptContract(contractId: number): void {
    this.isProcessingContract = true;
    this.contractService.acceptContract(contractId).subscribe({
      next: () => {
        this.toast.success('Contrato aceito com sucesso!');
        this.isProcessingContract = false;
        this.loadContracts();
      },
      error: () => {
        this.toast.error('Erro ao aceitar contrato.');
        this.isProcessingContract = false;
      }
    });
  }

  rejectContract(contractId: number): void {
    this.isProcessingContract = true;
    this.contractService.rejectContract(contractId).subscribe({
      next: () => {
        this.toast.success('Contrato recusado.');
        this.isProcessingContract = false;
        this.loadContracts();
      },
      error: () => {
        this.toast.error('Erro ao recusar contrato.');
        this.isProcessingContract = false;
      }
    });
  }

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      PENDING: 'Pendente',
      ACTIVE: 'Ativo',
      FINISHED: 'Finalizado',
      CANCELLED: 'Cancelado'
    };
    return labels[status] ?? status;
  }

  private scrollToBottom(): void {
    try {
      const el = this.messagesContainer?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch (_) {
      // Scroll is best-effort; ignore errors if element is not yet rendered
    }
  }

  goBack(): void {
    this.router.navigate(['/chats']);
  }
}

