import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, map, Observable, tap} from 'rxjs';
import {RoommateRecommendation} from '../models/roommate-recommendation';
import {environment} from '../../enviroments/enviroment';

const IGNORED_STORAGE_KEY = 'roomie_ignored_recommendations';

@Injectable({
  providedIn: 'root'
})
export class RecommendationService {
  private readonly apiUrl = `${environment.apiUrl}/recommendations`;

  private readonly ignoredIdsSubject = new BehaviorSubject<Set<number>>(this.loadIgnoredIds());
  readonly ignoredIds$ = this.ignoredIdsSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  /**
   * Busca as recomendações de colegas de quarto do backend.
   */
  getRoommateRecommendations(): Observable<RoommateRecommendation[]> {
    return this.http.get<RoommateRecommendation[]>(`${this.apiUrl}/roommates`);
  }

  /**
   * Busca as recomendações já filtrando as que foram ignoradas pelo usuário.
   */
  getFilteredRecommendations(): Observable<RoommateRecommendation[]> {
    return this.getRoommateRecommendations().pipe(
      map(recommendations => {
        const ignored = this.ignoredIdsSubject.value;
        return recommendations.filter(r => !ignored.has(r.studentId));
      })
    );
  }

  /**
   * Marca um estudante como "ignorado". A recomendação não aparecerá mais
   * até o usuário desfazer ou a sessão expirar.
   */
  ignoreRecommendation(studentId: number): void {
    const current = this.ignoredIdsSubject.value;
    const updated = new Set(current);
    updated.add(studentId);
    this.ignoredIdsSubject.next(updated);
    this.persistIgnoredIds(updated);
  }

  /**
   * Remove um estudante da lista de ignorados, revertendo a ação.
   */
  undoIgnore(studentId: number): void {
    const current = this.ignoredIdsSubject.value;
    const updated = new Set(current);
    updated.delete(studentId);
    this.ignoredIdsSubject.next(updated);
    this.persistIgnoredIds(updated);
  }

  /**
   * Verifica se um estudante está na lista de ignorados.
   */
  isIgnored(studentId: number): boolean {
    return this.ignoredIdsSubject.value.has(studentId);
  }

  /**
   * Limpa toda a lista de ignorados.
   */
  clearIgnored(): void {
    this.ignoredIdsSubject.next(new Set());
    sessionStorage.removeItem(IGNORED_STORAGE_KEY);
  }

  /**
   * Retorna a quantidade de recomendações ignoradas.
   */
  get ignoredCount(): number {
    return this.ignoredIdsSubject.value.size;
  }

  // --- Persistência via sessionStorage ---

  private loadIgnoredIds(): Set<number> {
    try {
      const stored = sessionStorage.getItem(IGNORED_STORAGE_KEY);
      if (stored) {
        const ids: number[] = JSON.parse(stored);
        return new Set(ids);
      }
    } catch {
      sessionStorage.removeItem(IGNORED_STORAGE_KEY);
    }
    return new Set();
  }

  private persistIgnoredIds(ids: Set<number>): void {
    sessionStorage.setItem(IGNORED_STORAGE_KEY, JSON.stringify([...ids]));
  }
}
