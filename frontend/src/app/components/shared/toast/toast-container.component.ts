import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, Toast } from '../../../services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container" aria-live="polite" aria-atomic="true">
      @for (toast of toasts$ | async; track toast.id) {
        <div
          class="toast toast-{{ toast.type }}"
          [class.toast-removing]="toast.removing"
          role="alert"
        >
          <span class="toast-icon">{{ iconFor(toast.type) }}</span>
          <span class="toast-message">{{ toast.message }}</span>
          <button class="toast-close" (click)="dismiss(toast.id)" aria-label="Fechar">&times;</button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 1.25rem;
      right: 1.25rem;
      z-index: 10000;
      display: flex;
      flex-direction: column;
      gap: 0.625rem;
      max-width: 26rem;
      pointer-events: none;
    }

    .toast {
      display: flex;
      align-items: center;
      gap: 0.625rem;
      padding: 0.875rem 1.125rem;
      border-radius: 0.5rem;
      color: #fff;
      font-size: 0.925rem;
      line-height: 1.4;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.18);
      pointer-events: auto;
      animation: toast-slide-in 0.35s ease-out;
      transition: opacity 0.3s ease, transform 0.3s ease;
    }

    .toast-removing {
      opacity: 0;
      transform: translateX(100%);
    }

    .toast-success {
      background-color: #16a34a;
    }

    .toast-error {
      background-color: #dc2626;
    }

    .toast-info {
      background-color: #2563eb;
    }

    .toast-warning {
      background-color: #d97706;
    }

    .toast-icon {
      font-size: 1.25rem;
      flex-shrink: 0;
    }

    .toast-message {
      flex: 1;
    }

    .toast-close {
      background: none;
      border: none;
      color: inherit;
      font-size: 1.25rem;
      cursor: pointer;
      opacity: 0.8;
      padding: 0 0.25rem;
      line-height: 1;
      flex-shrink: 0;
    }

    .toast-close:hover {
      opacity: 1;
    }

    @keyframes toast-slide-in {
      from {
        opacity: 0;
        transform: translateX(100%);
      }
      to {
        opacity: 1;
        transform: translateX(0);
      }
    }

    @media (max-width: 480px) {
      .toast-container {
        top: auto;
        bottom: 1rem;
        right: 0.75rem;
        left: 0.75rem;
        max-width: none;
      }
    }
  `],
})
export class ToastContainerComponent {
  private readonly toastService = inject(ToastService);
  readonly toasts$ = this.toastService.toasts;

  iconFor(type: string): string {
    switch (type) {
      case 'success': return '✓';
      case 'error':   return '✗';
      case 'info':    return 'ℹ';
      case 'warning': return '⚠';
      default:        return '';
    }
  }

  dismiss(id: number): void {
    this.toastService.dismiss(id);
  }
}
