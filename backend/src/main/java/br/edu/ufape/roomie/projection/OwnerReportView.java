package br.edu.ufape.roomie.projection;

public interface OwnerReportView {
    Long getIdProprietario();

    String getNomeProprietario();

    String getEmailProprietario();

    Long getTotalImoveis();

    Long getTotalVagasOferecidas();

    Double getMediaPrecoImoveis();

    Double getMenorPreco();

    Double getMaiorPreco();
}
