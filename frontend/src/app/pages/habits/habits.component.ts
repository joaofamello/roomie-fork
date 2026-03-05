import {Component, inject, OnInit, ChangeDetectorRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Router} from '@angular/router';
import {HabitService} from '../../services/habit.service';
import {HabitRequest} from '../../models/habit';
import {HeaderComponent} from '../../components/shared/header/header.component';
import {ToastService} from '../../services/toast.service';

interface HabitOption {
  label: string;
  value: string;
  icon: string;
}

@Component({
  selector: 'app-habits',
  standalone: true,
  imports: [CommonModule, HeaderComponent],
  templateUrl: './habits.component.html',
  styleUrl: './habits.component.css',
})
export class HabitsComponent implements OnInit {
  private readonly habitService = inject(HabitService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly toast = inject(ToastService);

  isLoading = true;
  isSaving = false;
  hasExistingHabits = false;

  // Seleções
  selectedSchedule: string | null = null;
  selectedHobbies = new Set<string>();
  selectedLifeStyles = new Set<string>();
  selectedCleaningPrefs = new Set<string>();

  // Opções disponíveis
  scheduleOptions: HabitOption[] = [
    {label: 'Manhã', value: 'MORNING', icon: 'wb_sunny'},
    {label: 'Tarde', value: 'AFTERNOON', icon: 'wb_twilight'},
    {label: 'Noite', value: 'EVENING', icon: 'nights_stay'},
    {label: 'Madrugada', value: 'DAWN', icon: 'dark_mode'},
  ];

  hobbyOptions: HabitOption[] = [
    {label: 'Leitura', value: 'Leitura', icon: 'menu_book'},
    {label: 'Esportes', value: 'Esportes', icon: 'sports_soccer'},
    {label: 'Games', value: 'Games', icon: 'sports_esports'},
    {label: 'Música', value: 'Música', icon: 'music_note'},
    {label: 'Filmes e Séries', value: 'Filmes e Séries', icon: 'movie'},
    {label: 'Cozinhar', value: 'Cozinhar', icon: 'restaurant'},
    {label: 'Arte', value: 'Arte', icon: 'palette'},
    {label: 'Viagens', value: 'Viagens', icon: 'flight'},
    {label: 'Fotografia', value: 'Fotografia', icon: 'photo_camera'},
    {label: 'Dança', value: 'Dança', icon: 'nightlife'},
  ];

  lifeStyleOptions: HabitOption[] = [
    {label: 'Introvertido', value: 'Introvertido', icon: 'person'},
    {label: 'Extrovertido', value: 'Extrovertido', icon: 'groups'},
    {label: 'Noturno', value: 'Noturno', icon: 'bedtime'},
    {label: 'Diurno', value: 'Diurno', icon: 'light_mode'},
    {label: 'Fitness', value: 'Fitness', icon: 'fitness_center'},
    {label: 'Vegetariano', value: 'Vegetariano', icon: 'eco'},
    {label: 'Fumante', value: 'Fumante', icon: 'smoking_rooms'},
    {label: 'Não fumante', value: 'Não fumante', icon: 'smoke_free'},
    {label: 'Pet friendly', value: 'Pet friendly', icon: 'pets'},
    {label: 'Festeiro', value: 'Festeiro', icon: 'celebration'},
  ];

  cleaningPrefsOptions: HabitOption[] = [
    {label: 'Muito organizado', value: 'Muito organizado', icon: 'inventory_2'},
    {label: 'Organizado', value: 'Organizado', icon: 'check_box'},
    {label: 'Moderado', value: 'Moderado', icon: 'balance'},
    {label: 'Relaxado', value: 'Relaxado', icon: 'weekend'},
    {label: 'Limpa diariamente', value: 'Limpa diariamente', icon: 'cleaning_services'},
    {label: 'Limpa semanalmente', value: 'Limpa semanalmente', icon: 'event_repeat'},
    {label: 'Divide tarefas', value: 'Divide tarefas', icon: 'handshake'},
  ];

  ngOnInit(): void {
    this.loadExistingHabits();
  }

  loadExistingHabits(): void {
    this.isLoading = true;
    this.habitService.getMyHabits().subscribe({
      next: (habit) => {
        if (habit) {
          this.hasExistingHabits = true;
          this.selectedSchedule = habit.studySchedule;
          this.selectedHobbies = new Set(habit.hobbies ?? []);
          this.selectedLifeStyles = new Set(habit.lifeStyles ?? []);
          this.selectedCleaningPrefs = new Set(habit.cleaningPrefs ?? []);
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        // Erro na requisição — sem hábitos ou sem perfil de estudante
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  toggleSchedule(value: string): void {
    this.selectedSchedule = this.selectedSchedule === value ? null : value;
  }

  toggleSelection(set: Set<string>, value: string): void {
    if (set.has(value)) {
      set.delete(value);
    } else {
      set.add(value);
    }
  }

  isSelected(set: Set<string>, value: string): boolean {
    return set.has(value);
  }

  get hasAnySelection(): boolean {
    return this.selectedSchedule !== null
      || this.selectedHobbies.size > 0
      || this.selectedLifeStyles.size > 0
      || this.selectedCleaningPrefs.size > 0;
  }

  get totalSelections(): number {
    return (this.selectedSchedule ? 1 : 0)
      + this.selectedHobbies.size
      + this.selectedLifeStyles.size
      + this.selectedCleaningPrefs.size;
  }

  onSave(): void {
    this.isSaving = true;

    const dto: HabitRequest = {
      studySchedule: this.selectedSchedule,
      hobbies: [...this.selectedHobbies],
      lifeStyles: [...this.selectedLifeStyles],
      cleaningPrefs: [...this.selectedCleaningPrefs],
    };

    this.habitService.saveHabits(dto).subscribe({
      next: () => {
        this.isSaving = false;
        this.hasExistingHabits = true;
        this.toast.success('Hábitos salvos com sucesso!');
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isSaving = false;
        const msg = err.error?.message || err.error || 'Erro ao salvar hábitos. Tente novamente.';
        this.toast.error(typeof msg === 'string' ? msg : 'Erro ao salvar hábitos.');
        this.cdr.detectChanges();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/student-profile']);
  }

  goToRecommendations(): void {
    this.router.navigate(['/recommendations']);
  }
}
