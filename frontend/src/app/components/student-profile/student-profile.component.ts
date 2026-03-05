import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormControl } from '@angular/forms';
import { Router } from '@angular/router';
import { Auth } from '../../auth/auth';
import { StudentService } from '../../services/student.service';
import { UserService } from '../../services/user.service';
import { HeaderComponent } from '../shared/header/header.component';
import { take } from 'rxjs';

@Component({
  selector: 'app-student-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HeaderComponent],
  templateUrl: './student-profile.component.html',
  styleUrls: ['./student-profile.component.css']
})
export class StudentProfileComponent implements OnInit {
  studentForm!: FormGroup;
  newPhoneControl = new FormControl('', Validators.pattern(/^[\d\s()+-]{7,20}$/));

  phones: string[] = [];
  isLoading = false;
  isSavingPhones = false;
  hasProfile = false;
  successMessage = '';
  errorMessage = '';
  phonesSuccess = '';
  phonesError = '';

  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(Auth);
  private readonly studentService = inject(StudentService);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  ngOnInit(): void {
    this.studentForm = this.fb.group({
      institution: ['', [Validators.required, Validators.minLength(3)]],
      major: ['', [Validators.required, Validators.minLength(3)]]
    });
    this.loadContactData();
  }

  loadContactData(): void {
    this.auth.currentUser$.pipe(take(1)).subscribe(user => {
      if (!user) return;
      this.studentService.getById(user.id).subscribe({
        next: (contact) => {
          this.hasProfile = true;
          this.studentForm.patchValue({
            institution: contact.instituicao ?? '',
            major: contact.curso ?? ''
          });
          this.phones = contact.telefones
            ? contact.telefones.split(',').map(p => p.trim()).filter(Boolean)
            : [];
          this.cdr.detectChanges();
        },
        error: () => { /* estudante ainda não tem perfil */
        }
      });
    });
  }

  onSubmit(): void {
    if (this.studentForm.invalid) {
      this.studentForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.auth.currentUser$.pipe(take(1)).subscribe(user => {
      if (!user) {
        this.router.navigate(['/login']);
        return;
      }

      if (!user.id) {
        this.errorMessage = 'Sessão expirada. Por favor, faça login novamente.';
        this.isLoading = false;
        setTimeout(() => {
          this.auth.logout();
          this.router.navigate(['/login']);
        }, 2000);
        return;
      }

      const dto = {
        userId: user.id,
        institution: this.studentForm.value.institution,
        major: this.studentForm.value.major
      };

      // Tenta criar; se já existir, atualiza
      this.studentService.createProfile(dto).subscribe({
        next: () => {
          this.isLoading = false;
          this.hasProfile = true;
          this.successMessage = 'Perfil de estudante salvo com sucesso!';
          this.cdr.detectChanges();
        },
        error: () => {
          // Perfil já existe — tenta atualizar
          this.studentService.updateProfile(dto).subscribe({
            next: () => {
              this.isLoading = false;
              this.successMessage = 'Perfil de estudante atualizado com sucesso!';
              this.cdr.detectChanges();
            },
            error: () => {
              this.isLoading = false;
              this.errorMessage = 'Erro ao salvar perfil de estudante. Tente novamente.';
              this.cdr.detectChanges();
            }
          });
        }
      });
    });
  }

  goBack(): void {
    this.router.navigate(['/home']);
  }

  addPhone(): void {
    const num = this.newPhoneControl.value?.trim();
    if (!num || this.newPhoneControl.invalid) return;
    if (!this.phones.includes(num)) {
      this.phones = [...this.phones, num];
    }
    this.newPhoneControl.reset();
    this.cdr.detectChanges();
  }

  removePhone(index: number): void {
    this.phones = this.phones.filter((_, i) => i !== index);
    this.cdr.detectChanges();
  }

  savePhones(): void {
    this.isSavingPhones = true;
    this.phonesSuccess = '';
    this.phonesError = '';

    this.userService.updateProfile({phones: this.phones}).subscribe({
      next: () => {
        this.isSavingPhones = false;
        this.phonesSuccess = 'Telefones salvos com sucesso!';
        this.cdr.detectChanges();
      },
      error: () => {
        this.isSavingPhones = false;
        this.phonesError = 'Erro ao salvar telefones. Tente novamente.';
        this.cdr.detectChanges();
      }
    });
  }
}

