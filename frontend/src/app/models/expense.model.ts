export interface ExpenseRequest {
  propertyId: number;
  description: string;
  amount: number;
  expenseDate: string;
}

export interface ExpenseResponse {
  id: number;
  description: string;
  amount: number;
  expenseDate: string;
  registeredById: number;
}

export interface ExpenseSummary {
  expenses: ExpenseResponse[];
  totalAmount: number;
  numberOfResidents: number;
  amountPerResident: number;
}
