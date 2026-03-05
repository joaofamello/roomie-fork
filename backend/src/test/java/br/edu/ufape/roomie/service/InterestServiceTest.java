package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.InterestSummaryDTO;
import br.edu.ufape.roomie.enums.InterestStatus;
import br.edu.ufape.roomie.model.Interest;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.InterestRepository;
import br.edu.ufape.roomie.repository.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InterestService interestService;

    private Student student;
    private User owner;
    private User notOwner;
    private Property property;
    private Interest interest;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setName("Estudante Teste");
        student.setMajor("Ciência da Computação");
        student.setInstitution("UFAPE");

        owner = new User();
        owner.setId(2L);
        owner.setName("Dono do Imóvel");

        notOwner = new User();
        notOwner.setId(3L);
        notOwner.setName("Outro Usuário");

        property = new Property();
        property.setId(100L);
        property.setOwner(owner);
        property.setTitle("Quarto perto da UFAPE");

        interest = new Interest(student, property);
        interest.setId(10L);
        interest.setInterestDate(LocalDateTime.now());
        interest.setStatus(InterestStatus.PENDING);
    }

    @Test
    @DisplayName("Deve registrar o interesse com sucesso e notificar o dono")
    void testaRegistrarInteresseComSucesso() {
        when(propertyRepository.findById(100L)).thenReturn(Optional.of(property));
        when(interestRepository.existsByStudentAndProperty(student, property)).thenReturn(false);

        interestService.registerInterest(100L, student);

        verify(interestRepository, times(1)).save(any(Interest.class));
        verify(notificationService, times(1)).notifyOwnerAboutInterest(owner, student, property);
    }

    @Test
    @DisplayName("Deve lançar exceção quando o estudante já demonstrou interesse no imóvel")
    void testaLancarExcecaoQuandoInteresseDuplicado() {
        when(propertyRepository.findById(100L)).thenReturn(Optional.of(property));
        when(interestRepository.existsByStudentAndProperty(student, property)).thenReturn(true);

        assertThatThrownBy(() -> interestService.registerInterest(100L, student))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Você já demonstrou interesse neste imóvel.");

        verify(interestRepository, never()).save(any(Interest.class));
        verify(notificationService, never()).notifyOwnerAboutInterest(any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o imóvel não for encontrado")
    void testaLancarExcecaoQuandoImovelNaoEncontrado() {
        when(propertyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestService.registerInterest(999L, student))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Imóvel não encontrado.");

        verify(interestRepository, never()).save(any(Interest.class));
        verify(notificationService, never()).notifyOwnerAboutInterest(any(), any(), any());
    }

    @Test
    @DisplayName("Deve listar os interessados com sucesso (Ficha Resumida) quando solicitado pelo proprietário")
    void testaListarInteressadosComSucesso() {
        when(propertyRepository.findById(100L)).thenReturn(Optional.of(property));
        when(interestRepository.findByPropertyId(100L)).thenReturn(List.of(interest));

        List<InterestSummaryDTO> result = interestService.listInterestsForProperty(100L, owner);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getInterestId()).isEqualTo(10L);
        assertThat(result.getFirst().getStudentName()).isEqualTo("Estudante Teste");
        assertThat(result.getFirst().getMajor()).isEqualTo("Ciência da Computação");
        assertThat(result.getFirst().getStatus()).isEqualTo(InterestStatus.PENDING);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar listar interessados se o usuário não for o dono do imóvel")
    void testaListarInteressadosAcessoNegado() {
        when(propertyRepository.findById(100L)).thenReturn(Optional.of(property));

        assertThatThrownBy(() -> interestService.listInterestsForProperty(100L, notOwner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Acesso negado: Apenas o proprietário pode visualizar os interessados.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao listar interessados de um imóvel inexistente")
    void testaListarInteressadosImovelNaoEncontrado() {
        when(propertyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestService.listInterestsForProperty(999L, owner))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Imóvel não encontrado.");
    }

    @Test
    @DisplayName("Deve atualizar o status com sucesso quando for o proprietário e a proposta estiver pendente")
    void testaAtualizarStatusComSucesso() {
        when(interestRepository.findById(10L)).thenReturn(Optional.of(interest));

        interestService.updateInterestStatus(10L, InterestStatus.ACCEPTED, owner);

        assertThat(interest.getStatus()).isEqualTo(InterestStatus.ACCEPTED);
        verify(interestRepository, times(1)).save(interest);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar status se o usuário não for o dono do imóvel")
    void testaAtualizarStatusAcessoNegado() {
        when(interestRepository.findById(10L)).thenReturn(Optional.of(interest));

        assertThatThrownBy(() -> interestService.updateInterestStatus(10L, InterestStatus.ACCEPTED, notOwner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Acesso negado: Apenas o proprietário pode alterar o status da proposta.");

        verify(interestRepository, never()).save(any(Interest.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar alterar uma proposta que já foi processada (Aceita ou Recusada)")
    void testaErroAlterarPropostaJaProcessada() {
        interest.setStatus(InterestStatus.ACCEPTED);
        when(interestRepository.findById(10L)).thenReturn(Optional.of(interest));

        assertThatThrownBy(() -> interestService.updateInterestStatus(10L, InterestStatus.REJECTED, owner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Não é possível alterar uma proposta que já foi processada.");

        verify(interestRepository, never()).save(any(Interest.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar status de um interesse inexistente")
    void testaAtualizarStatusInteresseNaoEncontrado() {
        when(interestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interestService.updateInterestStatus(999L, InterestStatus.ACCEPTED, owner))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Interesse não encontrado.");
    }
}