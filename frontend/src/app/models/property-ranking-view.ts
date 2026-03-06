export interface PropertyRankingView {
  idImovel: number;
  titulo: string;
  tipo: string;
  preco: number;
  status: string;
  cidade: string;
  bairro: string;
  nomeProprietario: string;
  totalAvaliacoes: number;
  mediaNota: number | null;
  piorNota: number | null;
  melhorNota: number | null;
}
