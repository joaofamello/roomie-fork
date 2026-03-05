package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.projection.OwnerReportView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    User findByCpf(String cpf);

    @Query(value = "SELECT id_proprietario AS idProprietario, nome_proprietario AS nomeProprietario, " +
            "email_proprietario AS emailProprietario, total_imoveis AS totalImoveis, " +
            "total_vagas_oferecidas AS totalVagasOferecidas, media_preco_imoveis AS mediaPrecoImoveis, " +
            "menor_preco AS menorPreco, maior_preco AS maiorPreco " +
            "FROM v_relatorio_proprietario", nativeQuery = true)
    List<OwnerReportView> findOwnerReports();
}
