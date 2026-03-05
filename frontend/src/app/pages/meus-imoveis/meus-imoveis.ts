import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { PropertyService } from '../../services/propertyService';
import { UserService } from '../../services/user.service';
import { Auth } from '../../auth/auth';
import { PropertyDetailView } from '../../models/property-detail-view';
import { OwnerReportView } from '../../models/owner-report-view';
import { HeaderComponent } from '../../components/shared/header/header.component';
import { take } from 'rxjs';

@Component({
  selector: 'app-meus-imoveis',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent],
  templateUrl: './meus-imoveis.html',
  styleUrl: './meus-imoveis.css',
})
export class MeusImoveis implements OnInit {

  properties: PropertyDetailView[] = [];
  ownerReport: OwnerReportView | null = null;
  isLoading = true;
  deleteConfirmId: number | null = null;

  constructor(
    private readonly propertyService: PropertyService,
    private readonly userService: UserService,
    private readonly auth: Auth,
    private readonly cdr: ChangeDetectorRef,
    private readonly router: Router
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
      error: (err: any) => {
        console.error('Erro ao buscar imóveis', err);
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
        error: (err: any) => {
          console.error('Erro ao buscar relatório', err);
          this.cdr.detectChanges();
        }
      });
    });
  }

  publish(id: number) {
    this.propertyService.publishProperty(id).subscribe({
      next: () => {
        this.loadProperties();
        this.loadOwnerReport();
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Erro ao publicar', err);
        this.cdr.detectChanges();
      }
    });
  }

  editProperty(id: number) {
    this.propertyService.setDraft(id).subscribe({
      next: () => {
        this.router.navigate(['/properties', id, 'edit']);
      },
      error: (err: any) => {
        console.error('Erro ao definir como rascunho', err);
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
        this.loadProperties();
        this.loadOwnerReport();
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Erro ao excluir imóvel', err);
        this.cdr.detectChanges();
      }
    });
  }
}
