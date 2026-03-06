import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PropertyService } from '../../services/propertyService';
import { StudentService } from '../../services/student.service';
import { PropertyRankingView } from '../../models/property-ranking-view';
import { StudentEngagementView } from '../../models/student-engagement-view';
import { HeaderComponent } from '../../components/shared/header/header.component';
import { ToastService } from '../../services/toast.service';

type Tab = 'ranking' | 'engajamento';

@Component({
  selector: 'app-relatorios',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent],
  templateUrl: './relatorios.html',
  styleUrl: './relatorios.css',
})
export class RelatoriosComponent implements OnInit {
  activeTab: Tab = 'ranking';

  ranking: PropertyRankingView[] = [];
  engajamento: StudentEngagementView[] = [];

  isLoadingRanking = true;
  isLoadingEngajamento = true;

  constructor(
    private readonly propertyService: PropertyService,
    private readonly studentService: StudentService,
    private readonly toast: ToastService,
  ) {}

  ngOnInit(): void {
    this.loadRanking();
    this.loadEngajamento();
  }

  setTab(tab: Tab): void {
    this.activeTab = tab;
  }

  loadRanking(): void {
    this.propertyService.getRanking().subscribe({
      next: (data) => {
        this.ranking = data;
        this.isLoadingRanking = false;
      },
      error: () => {
        this.toast.error('Erro ao carregar ranking de imóveis.');
        this.isLoadingRanking = false;
      },
    });
  }

  loadEngajamento(): void {
    this.studentService.getEngagement().subscribe({
      next: (data) => {
        this.engajamento = data;
        this.isLoadingEngajamento = false;
      },
      error: () => {
        this.toast.error('Erro ao carregar engajamento de estudantes.');
        this.isLoadingEngajamento = false;
      },
    });
  }

  tipoLabel(tipo: string): string {
    const map: Record<string, string> = {
      HOUSE: 'Casa',
      APARTMENT: 'Apartamento',
      STUDIO: 'Studio',
      ROOM: 'Quarto',
      DORMITORY: 'Pensionato',
    };
    return map[tipo] ?? tipo;
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      DRAFT: 'Rascunho',
      ACTIVE: 'Ativo',
      RENTED: 'Alugado',
    };
    return map[status] ?? status;
  }

  statusClass(status: string): string {
    const map: Record<string, string> = {
      DRAFT: 'badge-draft',
      ACTIVE: 'badge-active',
      RENTED: 'badge-rented',
    };
    return map[status] ?? '';
  }

  stars(nota: number | null): string {
    if (nota === null) return '—';
    return '★'.repeat(Math.round(nota)) + '☆'.repeat(5 - Math.round(nota));
  }
}
