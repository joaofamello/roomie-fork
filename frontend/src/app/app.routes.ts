import { Routes } from '@angular/router';
import { authGuard } from './auth/auth-guard';
import { Login } from './auth/login/login';
import { Unauthorized } from './auth/unauthorized/unauthorized';
import { Home } from './home/home';
import { PropertyFormComponent } from './property-form/property-form'; 

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
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: 'unauthorized',
    component: Unauthorized,
    title: 'Acesso Negado - Roomie'
  },
  {
    path: 'home',
    component: Home,
    canActivate: [authGuard],
    title: 'Início - Roomie'
  },

  {
    path: 'properties/new', 
    component: PropertyFormComponent,
    canActivate: [authGuard], 
    title: 'Novo Imóvel - Roomie'
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];