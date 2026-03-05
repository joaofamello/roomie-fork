package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.projection.PropertyDetailView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    @Query(value = "SELECT id_imovel AS idImovel, titulo, descricao, tipo, preco, " +
            "genero_moradores AS generoMoradores, aceita_animais AS aceitaAnimais, " +
            "tem_garagem AS temGaragem, vagas_disponiveis AS vagasDisponiveis, status, " +
            "rua, num_endereco AS numEndereco, bairro, cidade, estado, cep, " +
            "nome_proprietario AS nomeProprietario, email_proprietario AS emailProprietario " +
            "FROM v_detalhes_imovel", nativeQuery = true)
    List<PropertyDetailView> findAllDetails();

    @Query(value = "SELECT id_imovel AS idImovel, titulo, descricao, tipo, preco, " +
            "genero_moradores AS generoMoradores, aceita_animais AS aceitaAnimais, " +
            "tem_garagem AS temGaragem, vagas_disponiveis AS vagasDisponiveis, status, " +
            "rua, num_endereco AS numEndereco, bairro, cidade, estado, cep, " +
            "nome_proprietario AS nomeProprietario, email_proprietario AS emailProprietario " +
            "FROM v_detalhes_imovel WHERE id_imovel = :id", nativeQuery = true)
    Optional<PropertyDetailView> findDetailById(@Param("id") Long id);

    @Query(value = "SELECT p.* FROM imovel p LEFT JOIN endereco a ON p.id_imovel = a.id_imovel " +
            "WHERE p.status = 'ACTIVE' " +
            "AND (:location = '[ALL]' OR LOWER(a.cidade) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND (:district = '[ALL]' OR LOWER(a.bairro) LIKE LOWER(CONCAT('%', :district, '%'))) " +
            "AND (:minPrice < 0 OR p.preco >= :minPrice) " +
            "AND (:maxPrice < 0 OR p.preco <= :maxPrice) " +
            "AND (:type = '[ALL]' OR CAST(p.tipo AS VARCHAR) = :type) ",
            nativeQuery = true)
    List<Property> findWithFilters(
            @Param("location") String location,
            @Param("district") String district,
            @Param("minPrice") double minPrice,
            @Param("maxPrice") double maxPrice,
            @Param("type") String type
    );
    @Query(value = "SELECT id_imovel AS idImovel, titulo, descricao, tipo, preco, " +
            "genero_moradores AS generoMoradores, aceita_animais AS aceitaAnimais, " +
            "tem_garagem AS temGaragem, vagas_disponiveis AS vagasDisponiveis, status, " +
            "rua, num_endereco AS numEndereco, bairro, cidade, estado, cep, " +
            "nome_proprietario AS nomeProprietario, email_proprietario AS emailProprietario " +
            "FROM v_detalhes_imovel WHERE email_proprietario = :email", nativeQuery = true)
    List<PropertyDetailView> findMyDetails(@Param("email") String email);

}
