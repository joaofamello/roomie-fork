import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  inject,
  Input,
  Output,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Auth } from '../../../auth/auth';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})
export class HeaderComponent {
  /** Exibe o botão "Cadastrar Imóvel" ao lado do avatar */
  @Input() showCreateProperty = true;

  @Output() logoClicked = new EventEmitter<void>();
  isMenuOpen = false;
  private readonly auth = inject(Auth);
  user$ = this.auth.currentUser$;
  private readonly router = inject(Router);
  private readonly elRef = inject(ElementRef);

  /** Fecha o menu ao clicar fora do componente */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elRef.nativeElement.contains(event.target)) {
      this.isMenuOpen = false;
    }
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  /**
   * Keyboard handler on the toggle button:
   * Enter/Space → toggle, Escape → close,
   * ArrowDown → open and focus first item.
   */
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

  /**
   * Keyboard handler inside the dropdown:
   * ArrowDown/ArrowUp → navigate items, Escape → close and return focus to button.
   */
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

  /**
   * Handles empty / whitespace-only names gracefully.
   */
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

  goToStudentProfile(): void {
    this.isMenuOpen = false;
    this.router.navigate(['/student-profile']);
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
