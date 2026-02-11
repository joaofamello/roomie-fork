import { Routes } from '@angular/router';
import { authGuard } from './auth/auth-guard';
import { Login } from './auth/login/login';
import { Register } from './auth/register/register';
import { Unauthorized } from './auth/unauthorized/unauthorized';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    component: Login,
    title: 'Login - Roomie'
  },
  {
    path: 'register',
    component: Register,
    title: 'Cadastro - Roomie'
  },
  {
    path: 'unauthorized',
    component: Unauthorized,
    title: 'Acesso Negado - Roomie'
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
