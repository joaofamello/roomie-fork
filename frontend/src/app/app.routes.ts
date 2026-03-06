import {Routes} from '@angular/router';
import {authGuard} from './auth/auth-guard';
import {Login} from './auth/login/login';
import {Unauthorized} from './auth/unauthorized/unauthorized';
import {Home} from './home/home';
import {PropertyFormComponent} from './property-form/property-form';
import {ProfileEditComponent} from './components/profile/profile-edit.component';
import {StudentProfileComponent} from './components/student-profile/student-profile.component';
import {MeusImoveis} from './pages/meus-imoveis/meus-imoveis';
import {FavoritosComponent} from './pages/favoritos/favoritos.component';
import {PropertyDetailPageComponent} from './pages/property-detail/property-detail-page.component';
import {RecommendationsComponent} from './pages/recommendations/recommendations.component';
import {HabitsComponent} from './pages/habits/habits.component';
import {CandidatosDashboardComponent} from './pages/candidatos-dashboard/candidatos-dashboard';
import {ChatListComponent} from './pages/chat/chat-list/chat-list.component';
import {ChatDetailComponent} from './pages/chat/chat-detail/chat-detail.component';
import {RelatoriosComponent} from './pages/relatorios/relatorios';

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
    path: 'habits',
    component: HabitsComponent,
    canActivate: [authGuard],
    title: 'Meus Hábitos - Roomie'
  },
  {
    path: 'meus-imoveis',
    component: MeusImoveis,
    canActivate: [authGuard],
    title: 'Meus Imóveis - Roomie'
  },
  {
    path: 'favoritos',
    component: FavoritosComponent,
    canActivate: [authGuard],
    title: 'Favoritos - Roomie'
  },
  {
    path: 'recommendations',
    component: RecommendationsComponent,
    canActivate: [authGuard],
    title: 'Recomendações - Roomie'
  },
  {
    path: 'details/:id',
    component: PropertyDetailPageComponent,
    canActivate: [authGuard],
    title: 'Detalhes do Imóvel - Roomie'
  },
  {
    path: 'candidatos',
    component: CandidatosDashboardComponent,
    canActivate: [authGuard],
    title: 'Gerenciamento de Candidatos - Roomie'
  },
  {
    path: 'chats',
    component: ChatListComponent,
    canActivate: [authGuard],
    title: 'Conversas - Roomie'
  },
  {
    path: 'chats/:id',
    component: ChatDetailComponent,
    canActivate: [authGuard],
    title: 'Chat - Roomie'
  },
  {
    path: 'relatorios',
    component: RelatoriosComponent,
    canActivate: [authGuard],
    title: 'Relatórios - Roomie'
  },
  {
    path: '**',
    redirectTo: '/login'
  }

];
