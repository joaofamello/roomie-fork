import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Auth } from '../auth';
import { firstValueFrom } from 'rxjs';
import { RegisterData } from '../user.interface';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private fb = inject(FormBuilder);
  private auth = inject(Auth);
  private router = inject(Router);

  isRightPanelActive: boolean = false;
  showLoginPass: boolean = false;
  showRegisterPass: boolean = false;
  showConfirmPass: boolean = false;

  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  registerForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    cpf: ['', [Validators.required, this.cpfValidator]],
    phone: ['', [Validators.required, Validators.pattern('^[0-9]{10,11}$')]],
    gender: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: this.passwordMatchValidator });

  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;
    return password !== confirmPassword ? { passwordMismatch: true } : null;
  }

  cpfValidator(control: AbstractControl): ValidationErrors | null {
    const cpf = control.value?.replace(/\D/g, '');

    if (!cpf) return null;
    if (cpf.length !== 11 || /^(\d)\1+$/.test(cpf)) return { cpfInvalid: true };

    let sum = 0;
    let remainder;

    for (let i = 1; i <= 9; i++) sum = sum + parseInt(cpf.substring(i - 1, i)) * (11 - i);
    remainder = (sum * 10) % 11;
    if ((remainder === 10) || (remainder === 11)) remainder = 0;
    if (remainder !== parseInt(cpf.substring(9, 10))) return { cpfInvalid: true };

    sum = 0;
    for (let i = 1; i <= 10; i++) sum = sum + parseInt(cpf.substring(i - 1, i)) * (12 - i);
    remainder = (sum * 10) % 11;
    if ((remainder === 10) || (remainder === 11)) remainder = 0;
    if (remainder !== parseInt(cpf.substring(10, 11))) return { cpfInvalid: true };

    return null;
  }

  async onLogin() {
    if(this.loginForm.valid) {
      try {
        await firstValueFrom(this.auth.login(this.loginForm.value));
        await this.router.navigate(['/home']);
      } catch (error) {
        alert('Falha no login! Verifique suas credenciais.');
      }
    } else {
      this.loginForm.markAllAsTouched();
    }
  }

  async onRegister() {
    if (this.registerForm.valid) {
      try {
        const formValue = this.registerForm.value;

        const payload: RegisterData = {
          name: formValue.name,
          email: formValue.email,
          cpf: formValue.cpf.replace(/\D/g, ''),
          password: formValue.password,
          gender: formValue.gender,
          phones: [formValue.phone.replace(/\D/g, '')]
        };

        await firstValueFrom(this.auth.register(payload));

        alert('Cadastro realizado com sucesso! Faça login.');
        this.togglePanel();
        this.registerForm.reset();
      } catch (error: any) {
        console.error(error);
        const msg = error?.error?.message || 'Erro ao realizar cadastro.';
        alert(msg);
      }
    } else {
      this.registerForm.markAllAsTouched();
    }
  }

  togglePanel() {
    this.isRightPanelActive = !this.isRightPanelActive;
  }

  toggleLoginPass() { this.showLoginPass = !this.showLoginPass; }
  toggleRegisterPass() { this.showRegisterPass = !this.showRegisterPass; }
  toggleConfirmPass() { this.showConfirmPass = !this.showConfirmPass; }
}
