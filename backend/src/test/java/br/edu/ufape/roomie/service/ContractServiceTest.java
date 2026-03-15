package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.ContractRequestDTO;
import br.edu.ufape.roomie.dto.ContractResponseDTO;
import br.edu.ufape.roomie.enums.ContractStatus;
import br.edu.ufape.roomie.model.Chat;
import br.edu.ufape.roomie.model.Contract;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.ChatRepository;
import br.edu.ufape.roomie.repository.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private ContractService contractService;

    private Student student;
    private User owner;
    private Contract contract;
    private Chat chat;
    private Property property;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setName("Estudante Teste");

        owner = new User();
        owner.setId(2L);
        owner.setName("Proprietário Teste");

        property = new Property();
        property.setId(10L);
        property.setTitle("Casa Próxima a UFAPE");

        chat = new Chat();
        chat.setId(50L);
        chat.setStudent(student);
        chat.setUser(owner);
        chat.setProperty(property);

        contract = new Contract();
        contract.setIdContract(100L);
        contract.setStudent(student);
        contract.setOwner(owner);
        contract.setProperty(property);
        contract.setChat(chat);
        contract.setStatus(ContractStatus.PENDING);
        contract.setPrice(BigDecimal.valueOf(500.0));
        contract.setStartDate(LocalDate.now());
        contract.setEndDate(LocalDate.now().plusMonths(6));
    }

    // Contrato não encontrado
    @Test
    void acceptContract_ShouldThrowException_WhenContractNotFound() {
        when(contractRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            contractService.acceptContract(100L, student);
        });

        // Garante que o método save nunca foi chamado
        verify(contractRepository, never()).save(any()); 
    }

    // Usuário não é o estudante destinatário
    @Test
    void acceptContract_ShouldThrowException_WhenUserIsNotTheStudent() {
        User wrongStudent = new User();
        wrongStudent.setId(99L); // ID diferente do estudante do contrato (que é 1L)

        when(contractRepository.findById(100L)).thenReturn(Optional.of(contract));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            contractService.acceptContract(100L, wrongStudent);
        });

        assertEquals("Apenas o estudante destinatário pode aceitar o contrato.", exception.getMessage());
    }

    // Contrato não está pendente
    @Test
    void acceptContract_ShouldThrowException_WhenStatusIsNotPending() {
        contract.setStatus(ContractStatus.ACTIVE); // Mudando o status para cair no if

        when(contractRepository.findById(100L)).thenReturn(Optional.of(contract));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            contractService.acceptContract(100L, student);
        });

        assertEquals("Este contrato não está pendente.", exception.getMessage());
    }

    // Caminho de Sucesso
    @Test
    void acceptContract_ShouldAcceptContract_WhenAllValidationsPass() {
        when(contractRepository.findById(100L)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        ContractResponseDTO response = contractService.acceptContract(100L, student);

        assertNotNull(response);
        assertEquals(ContractStatus.ACTIVE, contract.getStatus()); // Verifica se o status mudou
        verify(contractRepository, times(1)).save(contract); // Verifica se salvou no banco
    }

    // Rejeição de Contrato
    
    // Falha se o contrato não existir
    @Test
    void rejectContract_ShouldThrowException_WhenContractNotFound() {
        when(contractRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            contractService.rejectContract(100L, student);
        });

        verify(contractRepository, never()).save(any()); 
    }

    // Falha se o requisitante não for o estudante 
    @Test
    void rejectContract_ShouldThrowException_WhenUserIsNotTheStudent() {
        User wrongStudent = new User();
        wrongStudent.setId(99L);

        when(contractRepository.findById(100L)).thenReturn(Optional.of(contract));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            contractService.rejectContract(100L, wrongStudent);
        });

        assertEquals("Apenas o estudante destinatário pode recusar o contrato.", exception.getMessage());
    }

    // Falha se o contrato não estiver pendente
    @Test
    void rejectContract_ShouldThrowException_WhenStatusIsNotPending() {
        contract.setStatus(ContractStatus.CANCELLED);

        when(contractRepository.findById(100L)).thenReturn(Optional.of(contract));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            contractService.rejectContract(100L, student);
        });

        assertEquals("Este contrato não está pendente.", exception.getMessage());
    }

    // Caminho de Sucesso
    @Test
    void rejectContract_ShouldRejectContract_WhenAllValidationsPass() {
        when(contractRepository.findById(100L)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        ContractResponseDTO response = contractService.rejectContract(100L, student);

        assertNotNull(response);
        assertEquals(ContractStatus.CANCELLED, contract.getStatus());
        verify(contractRepository, times(1)).save(contract);
    }

    // Criação de Contrato

    // Falha se o chat não existir
    @Test
    void createContract_ShouldThrowException_WhenChatNotFound() {
        ContractRequestDTO request = new ContractRequestDTO(50L, LocalDate.now(), LocalDate.now().plusMonths(6), BigDecimal.valueOf(500.0));
        when(chatRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            contractService.createContract(request, owner);
        });

        verify(contractRepository, never()).save(any());
    }

    // Falha se o criador do contrato não for o proprietário do imóvel
    @Test
    void createContract_ShouldThrowException_WhenUserIsNotOwner() {
        ContractRequestDTO request = new ContractRequestDTO(50L, LocalDate.now(), LocalDate.now().plusMonths(6), BigDecimal.valueOf(500.0));
        when(chatRepository.findById(50L)).thenReturn(Optional.of(chat));
        
        User wrongOwner = new User();
        wrongOwner.setId(99L); // Different from chat owner 2L

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            contractService.createContract(request, wrongOwner);
        });

        assertEquals("Apenas o proprietário do imóvel pode enviar uma proposta de contrato.", exception.getMessage());
    }

    // Caminho de Sucesso
    @Test
    void createContract_ShouldCreateContract_WhenAllValidationsPass() {
        ContractRequestDTO request = new ContractRequestDTO(50L, LocalDate.now(), LocalDate.now().plusMonths(6), BigDecimal.valueOf(600.0));
        when(chatRepository.findById(50L)).thenReturn(Optional.of(chat));
        when(contractRepository.save(any(Contract.class))).thenAnswer(i -> {
            Contract c = i.getArgument(0);
            c.setIdContract(200L);
            return c;
        });

        ContractResponseDTO response = contractService.createContract(request, owner);

        assertNotNull(response);
        assertEquals(ContractStatus.PENDING, response.getStatus());
        assertEquals(BigDecimal.valueOf(600.0), response.getPrice());
        verify(contractRepository, times(1)).save(any(Contract.class));
    }

    // Contratos por chat

    // Falha se o chat não existir
    @Test
    void getContractsByChat_ShouldThrowException_WhenChatNotFound() {
        when(chatRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            contractService.getContractsByChat(50L, owner);
        });
    }

    // Falha se o usuário não for nem estudante nem proprietário do chat
    @Test
    void getContractsByChat_ShouldThrowException_WhenUserNotInChat() {
        when(chatRepository.findById(50L)).thenReturn(Optional.of(chat));
        
        User outsider = new User();
        outsider.setId(99L);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            contractService.getContractsByChat(50L, outsider);
        });

        assertEquals("Acesso negado: Você não faz parte deste chat.", exception.getMessage());
    }

    // Caminho de Sucesso para estudante
    @Test
    void getContractsByChat_ShouldReturnContracts_WhenUserIsStudent() {
        when(chatRepository.findById(50L)).thenReturn(Optional.of(chat));
        when(contractRepository.findByChatId(50L)).thenReturn(java.util.List.of(contract));

        java.util.List<ContractResponseDTO> contracts = contractService.getContractsByChat(50L, student);

        assertNotNull(contracts);
        assertEquals(1, contracts.size());
        assertEquals(100L, contracts.get(0).getId());
    }

    // Caminho de Sucesso para proprietário
    @Test
    void getContractsByChat_ShouldReturnContracts_WhenUserIsOwner() {
        when(chatRepository.findById(50L)).thenReturn(Optional.of(chat));
        when(contractRepository.findByChatId(50L)).thenReturn(java.util.List.of(contract));

        java.util.List<ContractResponseDTO> contracts = contractService.getContractsByChat(50L, owner);

        assertNotNull(contracts);
        assertEquals(1, contracts.size());
        assertEquals(100L, contracts.get(0).getId());
    }
}