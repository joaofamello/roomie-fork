import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, tap} from 'rxjs';
import {jwtDecode} from 'jwt-decode';
import {LoginResponse, RegisterData, User, UserRole} from './user.interface';
import {environment} from '../../enviroments/enviroment';

@Injectable({
  providedIn: 'root'
})
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  private readonly currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    this.checkToken();
  }

  login(credentials: { email: string, password: string }): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {

        localStorage.setItem('token', response.token);

        this.setUserState(response.token);
      })
    );
  }

  register(data: RegisterData): Observable<User> {
    const payload = {
      ...data,
      role: 'USER'
    };
    return this.http.post<User>(`${this.apiUrl}/register`, payload);
  }

  logout(): void {
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem('token');
    if (!token) return false;

    try {
      const decoded: any = jwtDecode(token);
      const currentTime = Date.now() / 1000;

      if (decoded.exp && decoded.exp < currentTime) {
        this.logout();
        return false;
      }

      return true;
    } catch (e) {
      console.error(e);
      this.logout();
      return false;
    }
  }

  hasRole(requiredRole: UserRole): boolean {
    const user = this.currentUserSubject.value;
    return user?.role === requiredRole;
  }

  /**
   * Atualiza campos do usuário em memória sem precisar de novo login.
   *
   * ⚠️ IMPORTANTE: deve ser chamado APENAS após confirmação bem-sucedida
   * do backend (resposta 2xx). Nunca chame com dados não verificados pelo servidor,
   * pois é a única fonte de verdade para o estado de autenticação em memória.
   */
  updateCurrentUser(updates: Partial<User>): void {
    const current = this.currentUserSubject.value;
    if (current) {
      this.currentUserSubject.next({...current, ...updates});
    }
  }

  private checkToken() {
    const token = localStorage.getItem('token');
    if (token) {
      this.setUserState(token);
    }
  }

  private setUserState(token: string) {
    try {
      const decoded: any = jwtDecode(token);

      this.currentUserSubject.next({
        id: decoded.id,
        email: decoded.sub,
        name: decoded.name || 'Usuário',
        role: decoded.role as UserRole
      });
    } catch (e) {
      console.error(e);
      this.logout();
    }
  }
}
