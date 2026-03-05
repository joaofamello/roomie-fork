import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { PropertyService } from '../../services/propertyService';
import { InterestService } from '../../services/interest.service';
import { PropertyDetailView } from '../../models/property-detail-view';
import { InterestSummary } from '../../models/interest-summary';
import { InterestStatus } from '../../models/interest-status.enum';
import { HeaderComponent } from '../../components/shared/header/header.component';
import { ToastService } from '../../services/toast.service';

export type StatusFilter = 'ALL' | InterestStatus;

export interface PropertyWithInterests {
  property: PropertyDetailView;
  interests: InterestSummary[];
}

@Component({
  selector: 'app-candidatos-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent],
  templateUrl: './candidatos-dashboard.html',
  styleUrl: './candidatos-dashboard.css',
})
export class CandidatosDashboardComponent implements OnInit {
  readonly InterestStatus = InterestStatus;

  /** Dados carregados */
  propertyGroups: PropertyWithInterests[] = [];
  isLoading = true;
  hasError = false;

  /** Filtro ativo */
  activeStatusFilter: StatusFilter = 'ALL';
  activePropertyId: number | 'ALL' = 'ALL';

  constructor(
    private readonly propertyService: PropertyService,
    private readonly interestService: InterestService,
    private readonly cdr: ChangeDetectorRef,
    private readonly toast: ToastService,
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  /** Carrega todos os imóveis e, em seguida, os candidatos de cada um. */
  loadAll(): void {
    this.isLoading = true;
    this.hasError = false;
    this.cdr.detectChanges();

    this.propertyService.getMyProperties().subscribe({
      next: (properties: PropertyDetailView[]) => {
        if (properties.length === 0) {
          this.propertyGroups = [];
          this.isLoading = false;
          this.cdr.detectChanges();
          return;
        }

        const requests = properties.map(p =>
          this.interestService.getInterests(p.idImovel).pipe(
            catchError(() => of([] as InterestSummary[]))
          )
        );

        forkJoin(requests).subscribe({
          next: (allInterests: InterestSummary[][]) => {
            this.propertyGroups = properties
              .map((p, i) => ({ property: p, interests: allInterests[i] }))
              .filter(g => g.interests.length > 0);
            this.isLoading = false;
            this.cdr.detectChanges();
          },
          error: () => {
            this.toast.error('Erro ao carregar candidatos.');
            this.isLoading = false;
            this.hasError = true;
            this.cdr.detectChanges();
          }
        });
      },
      error: () => {
        this.toast.error('Erro ao carregar seus imóveis.');
        this.isLoading = false;
        this.hasError = true;
        this.cdr.detectChanges();
      }
    });
  }

  // ──────────────────────────────
  //  Filtros
  // ──────────────────────────────

  setStatusFilter(filter: StatusFilter): void {
    this.activeStatusFilter = filter;
  }

  setPropertyFilter(id: number | 'ALL'): void {
    this.activePropertyId = id;
  }

  /** Grupos filtrados conforme os filtros activos */
  get filteredGroups(): PropertyWithInterests[] {
    let groups = this.propertyGroups;

    if (this.activePropertyId !== 'ALL') {
      groups = groups.filter(g => g.property.idImovel === this.activePropertyId);
    }

    if (this.activeStatusFilter === 'ALL') {
      return groups;
    }

    return groups
      .map(g => ({
        ...g,
        interests: g.interests.filter(i => i.status === this.activeStatusFilter)
      }))
      .filter(g => g.interests.length > 0);
  }

  // ──────────────────────────────
  //  Estatísticas
  // ──────────────────────────────

  get totalCandidatos(): number {
    return this.propertyGroups.reduce((sum, g) => sum + g.interests.length, 0);
  }

  get totalPendentes(): number {
    return this.countByStatus(InterestStatus.PENDING);
  }

  get totalAceitos(): number {
    return this.countByStatus(InterestStatus.ACCEPTED);
  }

  get totalRecusados(): number {
    return this.countByStatus(InterestStatus.REJECTED);
  }

  private countByStatus(status: InterestStatus): number {
    return this.propertyGroups.reduce(
      (sum, g) => sum + g.interests.filter(i => i.status === status).length,
      0
    );
  }

  // ──────────────────────────────
  //  Ações
  // ──────────────────────────────

  updateCandidateStatus(interestId: number, status: InterestStatus, propertyId: number): void {
    this.interestService.updateInterestStatus(interestId, status).subscribe({
      next: () => {
        const label = status === InterestStatus.ACCEPTED ? 'aceito' : 'recusado';
        this.toast.success(`Candidato ${label} com sucesso!`);
        // Atualiza o status localmente sem refazer a requisição de listagem
        const group = this.propertyGroups.find(g => g.property.idImovel === propertyId);
        if (group) {
          const interest = group.interests.find(i => i.interestId === interestId);
          if (interest) {
            interest.status = status;
          }
        }
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        this.toast.error(err?.message ?? 'Não foi possível atualizar o status do candidato.');
        this.cdr.detectChanges();
      }
    });
  }

  // ──────────────────────────────
  //  Labels e classes auxiliares
  // ──────────────────────────────

  interestStatusLabel(status: InterestStatus): string {
    const map: Record<InterestStatus, string> = {
      [InterestStatus.PENDING]:  'Pendente',
      [InterestStatus.ACCEPTED]: 'Aceito',
      [InterestStatus.REJECTED]: 'Recusado',
    };
    return map[status] ?? status;
  }

  interestStatusClass(status: InterestStatus): string {
    const map: Record<InterestStatus, string> = {
      [InterestStatus.PENDING]:  'badge-pending',
      [InterestStatus.ACCEPTED]: 'badge-accepted',
      [InterestStatus.REJECTED]: 'badge-rejected',
    };
    return map[status] ?? '';
  }

  filterLabel(filter: StatusFilter): string {
    if (filter === 'ALL') return 'Todos';
    return this.interestStatusLabel(filter as InterestStatus);
  }

  readonly filterOptions: StatusFilter[] = [
    'ALL',
    InterestStatus.PENDING,
    InterestStatus.ACCEPTED,
    InterestStatus.REJECTED,
  ];
}
