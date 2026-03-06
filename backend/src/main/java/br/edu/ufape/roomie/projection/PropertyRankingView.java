package br.edu.ufape.roomie.projection;

public interface PropertyRankingView {
    Long getIdImovel();

    String getTitulo();

    String getTipo();

    Double getPreco();

    String getStatus();

    String getCidade();

    String getBairro();

    String getNomeProprietario();

    Long getTotalAvaliacoes();

    Double getMediaNota();

    Integer getPiorNota();

    Integer getMelhorNota();
}
