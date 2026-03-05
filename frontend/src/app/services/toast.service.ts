import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface Toast {
  id: number;
  message: string;
  type: ToastType;
  duration: number;
  removing?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private readonly toasts$ = new BehaviorSubject<Toast[]>([]);
  readonly toasts = this.toasts$.asObservable();
  private nextId = 0;

  /**
   * Exibe um toast de sucesso.
   * @param message Mensagem a exibir
   * @param duration Duração em ms (padrão: 4000)
   */
  success(message: string, duration = 4000): void {
    this.show(message, 'success', duration);
  }

  /**
   * Exibe um toast de erro.
   * @param message Mensagem a exibir
   * @param duration Duração em ms (padrão: 5000)
   */
  error(message: string, duration = 5000): void {
    this.show(message, 'error', duration);
  }

  /**
   * Exibe um toast informativo.
   * @param message Mensagem a exibir
   * @param duration Duração em ms (padrão: 4000)
   */
  info(message: string, duration = 4000): void {
    this.show(message, 'info', duration);
  }

  /**
   * Exibe um toast de aviso.
   * @param message Mensagem a exibir
   * @param duration Duração em ms (padrão: 4500)
   */
  warning(message: string, duration = 4500): void {
    this.show(message, 'warning', duration);
  }

  /**
   * Remove um toast pelo ID (com animação de saída).
   */
  dismiss(id: number): void {
    const current = this.toasts$.value;
    // Marca como "removing" para animação de saída
    this.toasts$.next(
      current.map((t) => (t.id === id ? { ...t, removing: true } : t))
    );
    // Remove após a animação
    setTimeout(() => {
      this.toasts$.next(this.toasts$.value.filter((t) => t.id !== id));
    }, 300);
  }

  private show(message: string, type: ToastType, duration: number): void {
    const id = this.nextId++;
    const toast: Toast = { id, message, type, duration };
    this.toasts$.next([...this.toasts$.value, toast]);

    if (duration > 0) {
      setTimeout(() => this.dismiss(id), duration);
    }
  }
}
