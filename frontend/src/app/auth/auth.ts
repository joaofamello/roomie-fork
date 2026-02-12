import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { LoginResponse, User, UserRole, RegisterData } from './user.interface';

@Injectable({
  providedIn: 'root'
})
export class Auth {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/auth';

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    this.checkToken();
  }

  login(credentials: {email: string, password: string}): Observable<LoginResponse> {
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
      this.logout();
      return false;
    }
  }

  hasRole(requiredRole: UserRole): boolean {
    const user = this.currentUserSubject.value;
    return user?.role === requiredRole;
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
      this.logout();
    }
  }
}
