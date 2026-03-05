import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Login } from './login';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { ReactiveFormsModule } from '@angular/forms';
import { Auth } from '../auth';
import { of } from 'rxjs';
import { LoginResponse } from '../user.interface';

describe('Login', () => {
  let component!: Login;
  let fixture!: ComponentFixture<Login>;
  let auth!: Auth;
  let router!: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login, ReactiveFormsModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;

    auth = TestBed.inject(Auth);
    router = TestBed.inject(Router);

    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('não deve chamar auth.login se o formulário estiver inválido (vazio)', async () => {
    const loginSpy = jest.spyOn(auth, 'login');

    await component.onLogin();

    expect(loginSpy).not.toHaveBeenCalled();
    expect(component.loginForm.touched).toBeTruthy();
  });

  it('deve chamar auth.login e navegar para /home com credenciais válidas', async () => {
    const mockTokenResponse: LoginResponse = { token: 'jwt-token-falso', role: 'USER' };
    const loginSpy = jest.spyOn(auth, 'login').mockReturnValue(of(mockTokenResponse));
    const navigateSpy = jest.spyOn(router, 'navigate').mockResolvedValue(true);

    component.loginForm.controls['email'].setValue('teste@ufape.edu.br');
    component.loginForm.controls['password'].setValue('123456'); // NOSONAR

    await component.onLogin();

    expect(loginSpy).toHaveBeenCalledWith({ email: 'teste@ufape.edu.br', password: '123456' }); // NOSONAR
    expect(navigateSpy).toHaveBeenCalledWith(['/home']);
  });

  it('deve formatar os dados corretamente e chamar auth.register com um formulário válido', async () => {
    const mockUserResponse = {
      id: 1,
      name: 'João Francisco',
      email: 'joao@ufape.edu.br',
      role: 'USER',
    };
    const registerSpy = jest.spyOn(auth, 'register').mockReturnValue(of(mockUserResponse as any));

    const alertSpy = jest.spyOn(globalThis, 'alert').mockImplementation(() => {});
    const togglePanelSpy = jest.spyOn(component, 'togglePanel');

    component.registerForm.controls['name'].setValue('João Francisco');
    component.registerForm.controls['email'].setValue('joao@ufape.edu.br');
    component.registerForm.controls['cpf'].setValue('123.456.789-09');
    component.registerForm.controls['phone'].setValue('81999999999');
    component.registerForm.controls['gender'].setValue('MALE');
    component.registerForm.controls['password'].setValue('senhaSegura123'); // NOSONAR
    component.registerForm.controls['confirmPassword'].setValue('senhaSegura123'); // NOSONAR

    await component.onRegister();

    expect(registerSpy).toHaveBeenCalledWith({
      name: 'João Francisco',
      email: 'joao@ufape.edu.br',
      cpf: '12345678909',
      phones: ['81999999999'],
      gender: 'MALE',
      password: 'senhaSegura123', // NOSONAR
    });

    expect(alertSpy).toHaveBeenCalledWith('Cadastro realizado com sucesso! Faça login.');
    expect(togglePanelSpy).toHaveBeenCalled();
  });
});
