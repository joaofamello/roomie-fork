import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroments/enviroment';
import { ExpenseRequest, ExpenseResponse, ExpenseSummary } from '../models/expense.model';

@Injectable({
  providedIn: 'root',
})
export class ExpenseService {
  private readonly apiUrl = `${environment.apiUrl}/api/expenses`;

  constructor(private readonly http: HttpClient) {}

  createExpense(data: ExpenseRequest): Observable<ExpenseResponse> {
    return this.http.post<ExpenseResponse>(this.apiUrl, data);
  }

  getExpensesByProperty(propertyId: number): Observable<ExpenseSummary> {
    return this.http.get<ExpenseSummary>(`${this.apiUrl}/property/${propertyId}`);
  }
}
