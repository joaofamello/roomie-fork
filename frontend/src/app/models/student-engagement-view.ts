export interface StudentEngagementView {
  idUsuario: number;
  nome: string;
  email: string;
  curso: string;
  instituicao: string;
  totalInteresses: number;
  totalContratos: number;
  totalAvaliacoesFeit: number;
  mediaNotaDada: number | null;
}
