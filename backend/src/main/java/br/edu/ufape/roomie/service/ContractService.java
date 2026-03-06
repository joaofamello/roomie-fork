package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.ContractRequestDTO;
import br.edu.ufape.roomie.dto.ContractResponseDTO;
import br.edu.ufape.roomie.enums.ContractStatus;
import br.edu.ufape.roomie.model.Chat;
import br.edu.ufape.roomie.model.Contract;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.ChatRepository;
import br.edu.ufape.roomie.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final ChatRepository chatRepository;

    @Transactional
    public ContractResponseDTO createContract(ContractRequestDTO request, User owner) {
        Chat chat = chatRepository.findById(request.getChatId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat não encontrado."));

        if (!chat.getUser().getId().equals(owner.getId())) {
            throw new IllegalStateException("Apenas o proprietário do imóvel pode enviar uma proposta de contrato.");
        }

        Contract contract = new Contract();
        contract.setProperty(chat.getProperty());
        contract.setStudent(chat.getStudent());
        contract.setOwner(owner);
        contract.setChat(chat);
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setPrice(request.getPrice());
        contract.setStatus(ContractStatus.PENDING);

        Contract saved = contractRepository.save(contract);
        return toDTO(saved);
    }

    @Transactional
    public ContractResponseDTO acceptContract(Long contractId, User student) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato não encontrado."));

        if (!contract.getStudent().getId().equals(student.getId())) {
            throw new IllegalStateException("Apenas o estudante destinatário pode aceitar o contrato.");
        }

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new IllegalStateException("Este contrato não está pendente.");
        }

        contract.setStatus(ContractStatus.ACTIVE);
        Contract saved = contractRepository.save(contract);
        return toDTO(saved);
    }

    @Transactional
    public ContractResponseDTO rejectContract(Long contractId, User student) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato não encontrado."));

        if (!contract.getStudent().getId().equals(student.getId())) {
            throw new IllegalStateException("Apenas o estudante destinatário pode recusar o contrato.");
        }

        if (contract.getStatus() != ContractStatus.PENDING) {
            throw new IllegalStateException("Este contrato não está pendente.");
        }

        contract.setStatus(ContractStatus.CANCELLED);
        Contract saved = contractRepository.save(contract);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ContractResponseDTO> getContractsByChat(Long chatId, User user) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat não encontrado."));

        boolean isOwner = chat.getUser().getId().equals(user.getId());
        boolean isStudent = chat.getStudent().getId().equals(user.getId());

        if (!isOwner && !isStudent) {
            throw new IllegalStateException("Acesso negado: Você não faz parte deste chat.");
        }

        return contractRepository.findByChatId(chatId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ContractResponseDTO toDTO(Contract contract) {
        return new ContractResponseDTO(
                contract.getIdContract(),
                contract.getProperty().getId(),
                contract.getProperty().getTitle(),
                contract.getStudent().getId(),
                contract.getStudent().getName(),
                contract.getOwner().getId(),
                contract.getOwner().getName(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getPrice(),
                contract.getStatus()
        );
    }
}
