import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { UpdateUserDto } from '../../models/user/update-user.dto';
import { HeaderComponent } from '../shared/header/header.component';
import { Auth } from '../../auth/auth';
import { take } from 'rxjs';

@Component({
  selector: 'app-profile-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HeaderComponent],
  templateUrl: './profile-edit.component.html',
  styleUrls: ['./profile-edit.component.css'],
})
export class ProfileEditComponent implements OnInit, OnDestroy {
  profileForm!: FormGroup;
  successMessage = '';
  errorMessage = '';
  isLoading = false;
  slowRequest = false;

  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly auth = inject(Auth);
  private slowTimer: number = 0;

  ngOnInit(): void {
    this.profileForm = this.fb.group({
      name: [''],
      email: [''],
      currentPassword: [''],
      newPassword: [''],
    });

    // Pré-carrega os dados do usuário logado no formulário
    this.auth.currentUser$.pipe(take(1)).subscribe((user) => {
      if (user) {
        this.profileForm.patchValue({
          name: user.name === 'Usuário' ? '' : user.name,
          email: user.email ?? '',
        });
      }
    });
  }

  ngOnDestroy(): void {
    clearTimeout(this.slowTimer);
  }

  onSubmit(): void {
    this.successMessage = '';
    this.errorMessage = '';

    // Validação client-side: nova senha exige senha atual
    const { newPassword, currentPassword } = this.profileForm.value;
    if (newPassword && !currentPassword) {
      this.errorMessage = 'Informe sua senha atual para definir uma nova senha.';
      return;
    }
    if (currentPassword && !newPassword) {
      this.errorMessage = 'Informe a nova senha ou deixe ambos os campos em branco.';
      return;
    }

    this.isLoading = true;
    this.slowRequest = false;

    // Avisa o usuário se a requisição demorar mais de 5 segundos (servidor frio)
    this.slowTimer = setTimeout(() => {
      if (this.isLoading) this.slowRequest = true;
    }, 5000);

    const formValues = this.profileForm.value;
    const dto: UpdateUserDto = {};

    if (formValues.name) dto.name = formValues.name;
    if (formValues.email) dto.email = formValues.email;
    if (formValues.newPassword) {
      dto.newPassword = formValues.newPassword;
      dto.currentPassword = formValues.currentPassword;
    }

    this.userService.updateProfile(dto).subscribe({
      next: (response) => {
        clearTimeout(this.slowTimer);
        this.isLoading = false;
        this.slowRequest = false;
        this.successMessage = 'Perfil atualizado com sucesso!';

        // Atualiza o estado em memória imediatamente — sem precisar de novo login
        this.auth.updateCurrentUser({
          name: response.name,
          email: response.email,
        });

        this.profileForm.get('currentPassword')?.reset();
        this.profileForm.get('newPassword')?.reset();
      },
      error: (err) => {
        clearTimeout(this.slowTimer);
        this.isLoading = false;
        this.slowRequest = false;

        const backendMsg = err?.error?.message ?? err?.error;
        if (typeof backendMsg === 'string' && backendMsg.length > 0) {
          this.errorMessage = backendMsg;
        } else if (err?.status === 400) {
          this.errorMessage = 'Dados inválidos. Verifique as informações e tente novamente.';
        } else if (err?.status === 0) {
          this.errorMessage = 'Não foi possível conectar ao servidor. Verifique sua conexão.';
        } else {
          this.errorMessage = 'Erro ao atualizar perfil. Tente novamente.';
        }
      },
    });
  }
}
