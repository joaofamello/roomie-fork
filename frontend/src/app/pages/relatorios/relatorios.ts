import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PropertyService } from '../../services/propertyService';
import { StudentService } from '../../services/student.service';
import { PropertyRankingView } from '../../models/property-ranking-view';
import { StudentEngagementView } from '../../models/student-engagement-view';
import { HeaderComponent } from '../../components/shared/header/header.component';
import { ToastService } from '../../services/toast.service';
import { ExpenseService } from '../../services/expense.service';
import { ExpenseRequest } from '../../models/expense.model';
import { PropertyDetailView } from '../../models/property-detail-view';

type Tab = 'ranking' | 'engajamento' | 'despesas';

@Component({
  selector: 'app-relatorios',
  standalone: true,
  imports: [CommonModule, RouterModule, HeaderComponent, ReactiveFormsModule],
  templateUrl: './relatorios.html',
  styleUrl: './relatorios.css',
})
export class RelatoriosComponent implements OnInit {
  activeTab: Tab = 'ranking';

  ranking: PropertyRankingView[] = [];
  engajamento: StudentEngagementView[] = [];

  isLoadingRanking = true;
  isLoadingEngajamento = true;

  expenseForm!: FormGroup;
  myProperties: PropertyDetailView[] = [];
  isLoadingProperties = true;
  isSubmittingExpense = false;
  expenseSubmitSuccess = '';
  expenseSubmitError = '';
  readonly maxExpenseDate = new Date().toISOString().split('T')[0];

  constructor(
    private readonly route: ActivatedRoute,
    private readonly fb: FormBuilder,
    private readonly propertyService: PropertyService,
    private readonly studentService: StudentService,
    private readonly expenseService: ExpenseService,
    private readonly toast: ToastService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['tab'] === 'despesas') {
        this.activeTab = 'despesas';
      }
    });

    this.initializeExpenseForm();
    this.loadRanking();
    this.loadEngajamento();
    this.loadMyProperties();
  }

  setTab(tab: Tab): void {
    this.activeTab = tab;
  }

  initializeExpenseForm(): void {
    this.expenseForm = this.fb.group({
      propertyId: [null, [Validators.required]],
      description: ['', [Validators.required, Validators.maxLength(160)]],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      expenseDate: [this.maxExpenseDate, [Validators.required]],
    });
  }

  loadMyProperties(): void {
    this.isLoadingProperties = true;
    this.propertyService.getMyProperties().subscribe({
      next: (properties: PropertyDetailView[]) => {
        this.myProperties = properties;
        this.isLoadingProperties = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.myProperties = [];
        this.isLoadingProperties = false;
        this.toast.error('Erro ao carregar imóveis para registro de despesas.');
        this.cdr.detectChanges();
      },
    });
  }

  isInvalid(controlName: string): boolean {
    const control = this.expenseForm.get(controlName);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  hasError(controlName: string, errorName: string): boolean {
    const control = this.expenseForm.get(controlName);
    return !!control && control.hasError(errorName) && (control.touched || control.dirty);
  }

  submitExpense(): void {
    if (this.expenseForm.invalid) {
      this.expenseForm.markAllAsTouched();
      this.expenseSubmitSuccess = '';
      this.expenseSubmitError = 'Preencha os campos obrigatórios corretamente.';
      return;
    }

    this.isSubmittingExpense = true;
    this.expenseSubmitSuccess = '';
    this.expenseSubmitError = '';

    const formValue = this.expenseForm.value;
    const payload: ExpenseRequest = {
      propertyId: Number(formValue.propertyId),
      description: String(formValue.description).trim(),
      amount: Number(formValue.amount),
      expenseDate: String(formValue.expenseDate),
    };

    this.expenseService.createExpense(payload).subscribe({
      next: () => {
        this.isSubmittingExpense = false;
        this.expenseSubmitSuccess = 'Despesa registrada com sucesso.';
        this.toast.success(this.expenseSubmitSuccess);

        this.expenseForm.reset({
          propertyId: null,
          description: '',
          amount: null,
          expenseDate: this.maxExpenseDate,
        });
        this.expenseForm.markAsPristine();
        this.expenseForm.markAsUntouched();
        this.cdr.detectChanges();
      },
      error: () => {
        this.isSubmittingExpense = false;
        this.expenseSubmitError = 'Não foi possível registrar a despesa. Tente novamente.';
        this.toast.error(this.expenseSubmitError);
        this.cdr.detectChanges();
      },
    });
  }

  loadRanking(): void {
    this.propertyService.getRanking().subscribe({
      next: (data: PropertyRankingView[]) => {
        this.ranking = data;
        this.isLoadingRanking = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toast.error('Erro ao carregar ranking de imóveis.');
        this.isLoadingRanking = false;
        this.cdr.detectChanges();
      },
    });
  }

  loadEngajamento(): void {
    this.studentService.getEngagement().subscribe({
      next: (data: StudentEngagementView[]) => {
        this.engajamento = data;
        this.isLoadingEngajamento = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.toast.error('Erro ao carregar engajamento de estudantes.');
        this.isLoadingEngajamento = false;
        this.cdr.detectChanges();
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
