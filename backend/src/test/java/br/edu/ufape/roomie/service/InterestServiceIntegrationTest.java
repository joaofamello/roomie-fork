package br.edu.ufape.roomie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.ufape.roomie.enums.InterestStatus;
import br.edu.ufape.roomie.enums.PropertyStatus;
import br.edu.ufape.roomie.enums.PropertyType;
import br.edu.ufape.roomie.enums.UserGender;
import br.edu.ufape.roomie.enums.UserRole;
import br.edu.ufape.roomie.model.Interest;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.InterestRepository;
import br.edu.ufape.roomie.repository.PropertyRepository;
import br.edu.ufape.roomie.repository.StudentRepository;
import br.edu.ufape.roomie.repository.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Disabled("requires proper test database; run manually when environment available")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InterestServiceIntegrationTest {

  @Autowired private InterestService interestService;

  @Autowired private InterestRepository interestRepository;

  @Autowired private PropertyRepository propertyRepository;

  @Autowired private StudentRepository studentRepository;

  @Autowired private UserRepository userRepository;

  private Student makeStudent(Long id) {
    Student s = new Student();
    s.setName("Aluno " + id);
    s.setEmail("aluno" + id + "@teste.com");
    s.setCpf("0000000000" + id);
    s.setPassword("senha");
    s.setGender(UserGender.MALE);
    s.setRole(UserRole.USER);
    s.setMajor("Curso");
    s.setInstitution("UFAPE");
    return studentRepository.save(s);
  }

  private User makeUser(Long id) {
    User u = new User();
    u.setName("Usuario " + id);
    u.setEmail("usuario" + id + "@teste.com");
    u.setCpf("1111111111" + id);
    u.setPassword("senha");
    u.setGender(UserGender.OTHER);
    u.setRole(UserRole.USER);
    return userRepository.save(u);
  }

  private Property makeProperty(User owner) {
    Property p = new Property();
    p.setOwner(owner);
    p.setTitle("Imovel teste");
    p.setType(PropertyType.ROOM);
    p.setPrice(new BigDecimal("100.00"));
    p.setGender(UserGender.MIXED);
    p.setAcceptAnimals(true);
    p.setHasGarage(false);
    p.setAvailableVacancies(1);
    p.setStatus(PropertyStatus.DRAFT);
    return propertyRepository.save(p);
  }

  @Test
  @DisplayName("Fluxo integrado: dono altera estado de interesse com sucesso")
  void fluxosDeSucesso() {
    Student student = makeStudent(1L);
    User owner = makeUser(2L);
    Property property = makeProperty(owner);

    Interest interest = new Interest(student, property);
    interest = interestRepository.save(interest);

    assertThat(interest.getStatus()).isEqualTo(InterestStatus.PENDING);

    interestService.updateInterestStatus(interest.getId(), InterestStatus.ACCEPTED, owner);

    Interest reloaded = interestRepository.findById(interest.getId()).get();
    assertThat(reloaded.getStatus()).isEqualTo(InterestStatus.ACCEPTED);
  }

  @Test
  @DisplayName("Integração: tentativa de alteração por usuário não proprietário falha")
  void integracaoAcessoNegado() {
    Student student = makeStudent(3L);
    User owner = makeUser(4L);
    User naoDono = makeUser(5L);
    Property property = makeProperty(owner);

    Interest interest = interestRepository.save(new Interest(student, property));

    assertThatThrownBy(
            () ->
                interestService.updateInterestStatus(
                    interest.getId(), InterestStatus.REJECTED, naoDono))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Acesso negado: Apenas o proprietário pode alterar o status da proposta.");
  }

  @Test
  @DisplayName("Integração: interesse inexistente gera exceção")
  void integracaoInteresseNaoEncontrado() {
    User owner = makeUser(6L);
    assertThatThrownBy(
            () -> interestService.updateInterestStatus(999L, InterestStatus.ACCEPTED, owner))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Interesse não encontrado.");
  }
}
