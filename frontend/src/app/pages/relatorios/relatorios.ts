import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { HeaderComponent } from '../../components/shared/header/header.component';
import { ExpenseRequest, ExpenseSummary } from '../../models/expense.model';
import { PropertyDetailView } from '../../models/property-detail-view';
import { PropertyRankingView } from '../../models/property-ranking-view';
import { StudentEngagementView } from '../../models/student-engagement-view';
import { ExpenseService } from '../../services/expense.service';
import { PropertyService } from '../../services/propertyService';
import { StudentService } from '../../services/student.service';
import { ToastService } from '../../services/toast.service';

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

  expenseSummary: ExpenseSummary | null = null;
  isLoadingSummary = false;

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

    this.expenseForm.get('propertyId')?.valueChanges.subscribe((propertyId) => {
      if (propertyId) {
        this.loadExpenseSummary(propertyId);
      } else {
        this.expenseSummary = null;
      }
    });
  }

  loadExpenseSummary(propertyId: number): void {
    this.isLoadingSummary = true;
    this.expenseSummary = null;
    this.expenseService.getExpensesByProperty(propertyId).subscribe({
      next: (summary) => {
        this.expenseSummary = summary;
        this.isLoadingSummary = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoadingSummary = false;
        this.toast.error('Erro ao carregar despesas da moradia.');
        this.cdr.detectChanges();
      }
    });
  }

  loadMyProperties(): void {
    this.isLoadingProperties = true;

    forkJoin({
      resident: this.propertyService.getMyResidentProperties(),
      owned: this.propertyService.getMyProperties()
    }).subscribe({
      next: ({ resident, owned }) => {
        // Remove duplicatas (caso um usuário seja de alguma forma dono e morador ao mesmo tempo)
        const allProperties = [...resident, ...owned];
        const uniquePropertiesMap = new Map<number, PropertyDetailView>();

        allProperties.forEach(prop => {
          uniquePropertiesMap.set(prop.idImovel, prop);
        });

        this.myProperties = Array.from(uniquePropertiesMap.values());
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

        // Reload the summary
        this.loadExpenseSummary(payload.propertyId);

        this.expenseForm.reset({
          propertyId: payload.propertyId, // Keep the same property selected
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
