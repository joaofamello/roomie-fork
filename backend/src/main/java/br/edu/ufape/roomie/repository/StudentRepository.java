package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.projection.StudentContactView;
import br.edu.ufape.roomie.projection.StudentEngagementView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query(value = "SELECT id_usuario AS idUsuario, nome, email, genero, curso, instituicao, telefones " +
            "FROM v_perfil_estudante_contato", nativeQuery = true)
    List<StudentContactView> findAllContacts();

    @Query(value = "SELECT id_usuario AS idUsuario, nome, email, genero, curso, instituicao, telefones " +
            "FROM v_perfil_estudante_contato WHERE id_usuario = :id", nativeQuery = true)
    Optional<StudentContactView> findContactById(@Param("id") Long id);

    @Query(value = "SELECT id_usuario AS idUsuario, nome, email, curso, instituicao, " +
            "total_interesses AS totalInteresses, total_contratos AS totalContratos, " +
            "total_avaliacoes_feitas AS totalAvaliacoesFeit, media_nota_dada AS mediaNotaDada " +
            "FROM v_engajamento_estudante", nativeQuery = true)
    List<StudentEngagementView> findAllEngagement();

    @Query(value = "SELECT id_usuario AS idUsuario, nome, email, curso, instituicao, " +
            "total_interesses AS totalInteresses, total_contratos AS totalContratos, " +
            "total_avaliacoes_feitas AS totalAvaliacoesFeit, media_nota_dada AS mediaNotaDada " +
            "FROM v_engajamento_estudante WHERE id_usuario = :id", nativeQuery = true)
    Optional<StudentEngagementView> findEngagementById(@Param("id") Long id);

    @Modifying
    @Query(value = "INSERT INTO estudante (id_estudante, curso, instituicao) VALUES (:id, :major, :institution)", nativeQuery = true)
    void promoteUserToStudent(@Param("id") Long id, @Param("major") String major, @Param("institution") String institution);

    @Modifying
    @Query(value = "UPDATE estudante SET curso = :major, instituicao = :institution WHERE id_estudante = :id", nativeQuery = true)
    int updateStudentProfile(@Param("id") Long id, @Param("major") String major, @Param("institution") String institution);

    List<Student> findByInstitution(String institution);

    List<Student> findByMajor(String major);
    List<Student> findByIdNot(Long id);

    Optional<Student> findByEmail(String email);

    Optional<Student> findByCpf(String cpf);
}
