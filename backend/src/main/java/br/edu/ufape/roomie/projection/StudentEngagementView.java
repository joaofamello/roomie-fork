package br.edu.ufape.roomie.projection;

public interface StudentEngagementView {
    Long getIdUsuario();

    String getNome();

    String getEmail();

    String getCurso();

    String getInstituicao();

    Long getTotalInteresses();

    Long getTotalContratos();

    Long getTotalAvaliacoesFeit();

    Double getMediaNotaDada();
}
