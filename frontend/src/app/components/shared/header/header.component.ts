import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  inject,
  Input,
  OnDestroy,
  OnInit,
  Output,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Auth } from '../../../auth/auth';
import { NotificationService } from '../../../services/notification.service';
import { AppNotification } from '../../../models/notification.model';
import { Subscription, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  /** Exibe o botão "Cadastrar Imóvel" ao lado do avatar */
  @Input() showCreateProperty = true;

  @Output() logoClicked = new EventEmitter<void>();
  isMenuOpen = false;
  isNotifOpen = false;
  notifications: AppNotification[] = [];

  private readonly auth = inject(Auth);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly elRef = inject(ElementRef);
  private pollSub?: Subscription;

  user$ = this.auth.currentUser$;

  get unreadCount(): number {
    return this.notifications.filter(n => !n.read).length;
  }

  ngOnInit(): void {
    this.loadNotifications();
    // Polling a cada 30 segundos
    this.pollSub = interval(30000).pipe(
      switchMap(() => this.notificationService.getNotifications())
    ).subscribe(notifications => {
      this.notifications = notifications;
    });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  loadNotifications(): void {
    this.notificationService.getNotifications().subscribe({
      next: notifications => (this.notifications = notifications),
      error: () => {}
    });
  }

  toggleNotif(): void {
    this.isNotifOpen = !this.isNotifOpen;
    this.isMenuOpen = false;
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe(() => {
      this.notifications = this.notifications.map(n => ({ ...n, read: true }));
    });
  }

  markAsRead(notif: AppNotification): void {
    if (notif.read) return;
    this.notificationService.markAsRead(notif.id).subscribe(() => {
      notif.read = true;
    });
  }

  /** Fecha o menu ao clicar fora do componente */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elRef.nativeElement.contains(event.target)) {
      this.isMenuOpen = false;
      this.isNotifOpen = false;
    }
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
    this.isNotifOpen = false;
  }

  onButtonKeydown(event: KeyboardEvent): void {
    switch (event.key) {
      case 'Enter':
      case ' ':
        event.preventDefault();
        this.toggleMenu();
        if (this.isMenuOpen) this.focusMenuItem(0);
        break;
      case 'ArrowDown':
        event.preventDefault();
        this.isMenuOpen = true;
        this.focusMenuItem(0);
        break;
      case 'Escape':
        this.isMenuOpen = false;
        break;
    }
  }

  onMenuKeydown(event: KeyboardEvent): void {
    const items = this.getMenuItems();
    const current = document.activeElement as HTMLElement;
    const idx = items.indexOf(current);

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.focusMenuItem(idx < items.length - 1 ? idx + 1 : 0);
        break;
      case 'ArrowUp':
        event.preventDefault();
        this.focusMenuItem(idx > 0 ? idx - 1 : items.length - 1);
        break;
      case 'Escape':
        event.preventDefault();
        this.isMenuOpen = false;
        this.elRef.nativeElement.querySelector('.user-button')?.focus();
        break;
      case 'Tab':
        // Fechar ao sair do menu com Tab
        this.isMenuOpen = false;
        break;
    }
  }

  getInitials(name: string): string {
    if (!name?.trim()) return 'U';
    const names = name.trim().split(' ');
    if (names.length === 1) return names[0].charAt(0).toUpperCase();
    return (names[0].charAt(0) + names.at(-1)!.charAt(0)).toUpperCase();
  }

  onLogoClick(): void {
    if (this.logoClicked.observers.length > 0) {
      this.logoClicked.emit();
    } else {
      this.router.navigate(['/home']);
    }
  }

  goToProfile(): void {
    this.isMenuOpen = false;
    this.router.navigate(['/profile']);
  }

  goToCreateProperty(): void {
    this.isMenuOpen = false;
    this.router.navigate(['/properties/new']);
  }

  goToMyProperties(): void {
    this.isMenuOpen = false;
    this.router.navigate(['/meus-imoveis']);
  }

  goToChats(): void {
    this.isMenuOpen = false;
    this.router.navigate(['/chats']);
  }

  goToFavorites(): void {
    this.isMenuOpen = false;
    this.router.navigate(['/favoritos']);
  }

  goToRecommendations(): void {
    this.isMenuOpen = false;
    this.router.navigate(['/recommendations']);
  }

  goToStudentProfile(): void {
    this.isMenuOpen = false;
    this.router.navigate(['/student-profile']);
  }

  goToHabits(): void {
    this.isMenuOpen = false;
    this.router.navigate(['/habits']);
  }

  onLogout(): void {
    this.isMenuOpen = false;
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  private getMenuItems(): HTMLElement[] {
    const nodeList: NodeListOf<HTMLElement> =
      this.elRef.nativeElement.querySelectorAll('[role="menuitem"]');
    return Array.from(nodeList);
  }

  private focusMenuItem(index: number): void {
    // Aguarda o DOM renderizar antes de focar
    setTimeout(() => {
      const items = this.getMenuItems();
      items[index]?.focus();
    });
  }
}
