import {Routes} from '@angular/router';
import {authGuard} from './auth/auth-guard';
import {Login} from './auth/login/login';
import {Unauthorized} from './auth/unauthorized/unauthorized';
import {Home} from './home/home';
import {PropertyFormComponent} from './property-form/property-form';
import {ProfileEditComponent} from './components/profile/profile-edit.component';
import {StudentProfileComponent} from './components/student-profile/student-profile.component';
import {MeusImoveis} from './pages/meus-imoveis/meus-imoveis';
import {PropertyDetailPageComponent} from './pages/property-detail/property-detail-page.component';

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
    title: 'Cadastrar Imóvel - Roomie'
  },
  {
    path: 'properties/:id/edit',
    component: PropertyFormComponent,
    canActivate: [authGuard],
    title: 'Editar Imóvel - Roomie'
  },
  {
    path: 'profile',
    component: ProfileEditComponent,
    canActivate: [authGuard],
    title: 'Meu Perfil - Roomie'
  },
  {
    path: 'student-profile',
    component: StudentProfileComponent,
    canActivate: [authGuard],
    title: 'Minha Instituição - Roomie'
  },
  {
    path: 'meus-imoveis',
    component: MeusImoveis,
    canActivate: [authGuard],
    title: 'Meus Imóveis - Roomie'
  },
  {
    path: 'details/:id',
    component: PropertyDetailPageComponent,
    canActivate: [authGuard],
    title: 'Detalhes do Imóvel - Roomie'
  },
  {
    path: '**',
    redirectTo: '/login'
  }

];
