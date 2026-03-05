package br.edu.ufape.roomie.projection;

public interface PropertyDetailView {
    Long getIdImovel();

    String getTitulo();

    String getDescricao();

    String getTipo();

    Double getPreco();

    String getGeneroMoradores();

    Boolean getAceitaAnimais();

    Boolean getTemGaragem();

    Integer getVagasDisponiveis();

    String getStatus();

    String getRua();

    Integer getNumEndereco();

    String getBairro();

    String getCidade();

    String getEstado();

    String getCep();

    String getNomeProprietario();

    String getEmailProprietario();
}
