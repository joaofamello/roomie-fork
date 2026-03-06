import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { PropertyService } from '../../services/propertyService';
import { UserService } from '../../services/user.service';
import { InterestService } from '../../services/interest.service';
import { ChatService } from '../../services/chat.service';
import { ChatWidgetService } from '../../services/chat-widget.service';
import { Auth } from '../../auth/auth';
import { PropertyDetailView } from '../../models/property-detail-view';
import { OwnerReportView } from '../../models/owner-report-view';
import { InterestSummary } from '../../models/interest-summary';
import { InterestStatus } from '../../models/interest-status.enum';
import { HeaderComponent } from '../../components/shared/header/header.component';
import { take } from 'rxjs';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-meus-imoveis',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent],
  templateUrl: './meus-imoveis.html',
  styleUrl: './meus-imoveis.css',
})
export class MeusImoveis implements OnInit {

  readonly InterestStatus = InterestStatus;

  properties: PropertyDetailView[] = [];
  ownerReport: OwnerReportView | null = null;
  isLoading = true;
  deleteConfirmId: number | null = null;

  /** ID do imóvel cujos candidatos estão sendo exibidos, ou null se nenhum */
  expandedInterestsPropertyId: number | null = null;
  /** Mapa de propertyId → lista de interessados já carregada */
  interestsMap: Map<number, InterestSummary[]> = new Map();
  isLoadingInterests = false;

  constructor(
    private readonly propertyService: PropertyService,
    private readonly userService: UserService,
    private readonly interestService: InterestService,
    private readonly chatService: ChatService,
    private readonly chatWidgetService: ChatWidgetService,
    private readonly auth: Auth,
    private readonly cdr: ChangeDetectorRef,
    private readonly router: Router,
    private readonly toast: ToastService
  ) {
  }

  ngOnInit(): void {
    this.loadProperties();
    this.loadOwnerReport();
  }

  loadProperties() {
    this.propertyService.getMyProperties().subscribe({
      next: (data: PropertyDetailView[]) => {
        this.properties = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toast.error('Erro ao carregar seus imóveis.');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  statusLabel(status: string | undefined): string {
    const map: Record<string, string> = {
      DRAFT: 'Rascunho',
      ACTIVE: 'Ativo',
      RENTED: 'Alugado'
    };
    return status ? (map[status] ?? status) : '';
  }

  statusClass(status: string | undefined): string {
    const map: Record<string, string> = {
      DRAFT: 'status-draft',
      ACTIVE: 'status-active',
      RENTED: 'status-rented'
    };
    return status ? (map[status] ?? '') : '';
  }

  typeLabel(type: string | undefined): string {
    const map: Record<string, string> = {
      HOUSE: 'Casa',
      APARTMENT: 'Apartamento',
      STUDIO: 'Studio',
      ROOM: 'Quarto',
      DORMITORY: 'República'
    };
    return type ? (map[type] ?? type) : '';
  }

  loadOwnerReport() {
    this.auth.currentUser$.pipe(take(1)).subscribe(currentUser => {
      if (!currentUser) return;
      this.userService.getOwnersReport().subscribe({
        next: (reports) => {
          this.ownerReport = reports.find(r => r.idProprietario === currentUser.id) ?? null;
          this.cdr.detectChanges();
        },
        error: () => {
          this.cdr.detectChanges();
        }
      });
    });
  }

  publish(id: number) {
    this.propertyService.publishProperty(id).subscribe({
      next: () => {
        this.toast.success('Imóvel publicado com sucesso!');
        this.loadProperties();
        this.loadOwnerReport();
        this.cdr.detectChanges();
      },
      error: () => {
        this.toast.error('Erro ao publicar o imóvel.');
        this.cdr.detectChanges();
      }
    });
  }

  editProperty(id: number) {
    this.propertyService.setDraft(id).subscribe({
      next: () => {
        this.router.navigate(['/properties', id, 'edit']);
      },
      error: () => {
        // Navigate to edit even if already draft
        this.router.navigate(['/properties', id, 'edit']);
      }
    });
  }

  askDelete(id: number) {
    this.deleteConfirmId = id;
  }

  cancelDelete() {
    this.deleteConfirmId = null;
  }

  confirmDelete() {
    if (this.deleteConfirmId === null) return;
    const id = this.deleteConfirmId;
    this.deleteConfirmId = null;
    this.propertyService.deleteProperty(id).subscribe({
      next: () => {
        this.toast.success('Imóvel excluído com sucesso!');
        this.loadProperties();
        this.loadOwnerReport();
        this.cdr.detectChanges();
      },
      error: () => {
        this.toast.error('Erro ao excluir o imóvel.');
        this.cdr.detectChanges();
      }
    });
  }

  /**
   * Alterna a exibição da lista de candidatos de um imóvel.
   * Na primeira abertura, busca os dados da API.
   */
  toggleInterests(propertyId: number): void {
    if (this.expandedInterestsPropertyId === propertyId) {
      this.expandedInterestsPropertyId = null;
      return;
    }

    this.expandedInterestsPropertyId = propertyId;

    if (this.interestsMap.has(propertyId)) {
      return; // já carregado
    }

    this.loadInterests(propertyId);
  }

  /**
   * Carrega a lista de estudantes interessados em um imóvel.
   * @param propertyId ID do imóvel
   */
  loadInterests(propertyId: number): void {
    this.isLoadingInterests = true;
    this.cdr.detectChanges();

    this.interestService.getInterests(propertyId).subscribe({
      next: (interests: InterestSummary[]) => {
        this.interestsMap.set(propertyId, interests);
        this.isLoadingInterests = false;
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => {
        this.toast.error(err?.message ?? 'Erro ao carregar candidatos.');
        this.isLoadingInterests = false;
        this.expandedInterestsPropertyId = null;
        this.cdr.detectChanges();
      }
    });
  }

  /**
   * Atualiza o status de um candidato (aceitar ou recusar).
   * @param interestId ID do registro de interesse
   * @param status     Novo status
   * @param propertyId ID do imóvel (para recarregar a lista após a atualização)
   */
  updateCandidateStatus(interestId: number, status: InterestStatus, propertyId: number): void {
    this.interestService.updateInterestStatus(interestId, status).subscribe({
      next: (_message: string) => {
        const label = status === InterestStatus.ACCEPTED ? 'aceito' : 'recusado';
        this.toast.success(`Candidato ${label} com sucesso!`);
        this.interestsMap.delete(propertyId);
        this.loadInterests(propertyId);
        this.cdr.detectChanges();
      },
      error: (err: HttpErrorResponse) => {
        this.toast.error(err?.message ?? 'Não foi possível atualizar o status do candidato.');
        this.cdr.detectChanges();
      }
    });
  }

  /** Retorna os candidatos de um imóvel já carregados, ou array vazio. */
  getInterestsFor(propertyId: number): InterestSummary[] {
    return this.interestsMap.get(propertyId) ?? [];
  }

  /** Label legível para o status de interesse */
  interestStatusLabel(status: InterestStatus): string {
    const map: Record<InterestStatus, string> = {
      [InterestStatus.PENDING]: 'Pendente',
      [InterestStatus.ACCEPTED]: 'Aceito',
      [InterestStatus.REJECTED]: 'Recusado'
    };
    return map[status] ?? status;
  }

  /** Classe CSS para o badge de status de interesse */
  interestStatusClass(status: InterestStatus): string {
    const map: Record<InterestStatus, string> = {
      [InterestStatus.PENDING]: 'interest-pending',
      [InterestStatus.ACCEPTED]: 'interest-accepted',
      [InterestStatus.REJECTED]: 'interest-rejected'
    };
    return map[status] ?? '';
  }

  /**
   * Inicia (ou reabre) um chat com um candidato e abre o widget de chat.
   */
  startChat(studentId: number, propertyId: number): void {
    this.chatService.startChat(studentId, propertyId).subscribe({
      next: (chat) => {
        this.chatWidgetService.openChat(chat.id);
      },
      error: () => {
        this.toast.error('Não foi possível iniciar a conversa.');
      }
    });
  }
}
