package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.InterestSummaryDTO;
import br.edu.ufape.roomie.enums.InterestStatus;
import br.edu.ufape.roomie.model.Interest;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.InterestRepository;
import br.edu.ufape.roomie.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;
    private final PropertyRepository propertyRepository;
    private final NotificationService notificationService;

    @Transactional
    public void registerInterest(Long propertyId, Student student) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));

        if (interestRepository.existsByStudentAndProperty(student, property)) {
            throw new IllegalStateException("Você já demonstrou interesse neste imóvel.");
        }

        Interest interest = new Interest(student, property);
        interestRepository.save(interest);

        try {
            notificationService.notifyOwnerAboutInterest(property.getOwner(), student, property);
        } catch (Exception e) {
            log.warn("Falha ao criar notificação para o proprietário (interesse salvo com sucesso): {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<InterestSummaryDTO> listInterestsForProperty(Long propertyId, User loggedInOwner) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));

        if (!property.getOwner().getId().equals(loggedInOwner.getId())) {
            throw new IllegalStateException("Acesso negado: Apenas o proprietário pode visualizar os interessados.");
        }

        List<Interest> interests = interestRepository.findByPropertyId(propertyId);

        return interests.stream().map(interest -> new InterestSummaryDTO(
                interest.getId(),
                interest.getStudent().getId(),
                interest.getStudent().getName(),
                interest.getStudent().getEmail(),
                interest.getStudent().getMajor(),
                interest.getStudent().getInstitution(),
                interest.getStatus(),
                interest.getInterestDate()
        )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean hasInterest(Long propertyId, Student student) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));
        return interestRepository.existsByStudentAndProperty(student, property);
    }

    @Transactional
    public void updateInterestStatus(Long interestId, InterestStatus newStatus, User loggedInOwner) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interesse não encontrado."));

        if (!interest.getProperty().getOwner().getId().equals(loggedInOwner.getId())) {
            throw new IllegalStateException("Acesso negado: Apenas o proprietário pode alterar o status da proposta.");
        }

        if (interest.getStatus() != InterestStatus.PENDING && interest.getStatus() != newStatus) {
            throw new IllegalStateException("Não é possível alterar uma proposta que já foi processada.");
        }

        interest.setStatus(newStatus);
        interestRepository.save(interest);
    }
}