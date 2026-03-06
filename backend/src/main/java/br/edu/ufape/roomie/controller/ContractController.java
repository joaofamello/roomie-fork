package br.edu.ufape.roomie.controller;

import br.edu.ufape.roomie.dto.ContractRequestDTO;
import br.edu.ufape.roomie.dto.ContractResponseDTO;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    public ResponseEntity<?> createContract(
            @RequestBody ContractRequestDTO request,
            @AuthenticationPrincipal User loggedInUser) {
        try {
            ContractResponseDTO contract = contractService.createContract(request, loggedInUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(contract);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<?> acceptContract(
            @PathVariable Long id,
            @AuthenticationPrincipal User loggedInUser) {
        try {
            ContractResponseDTO contract = contractService.acceptContract(id, loggedInUser);
            return ResponseEntity.ok(contract);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectContract(
            @PathVariable Long id,
            @AuthenticationPrincipal User loggedInUser) {
        try {
            ContractResponseDTO contract = contractService.rejectContract(id, loggedInUser);
            return ResponseEntity.ok(contract);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/chat/{chatId}")
    public ResponseEntity<?> getContractsByChat(
            @PathVariable Long chatId,
            @AuthenticationPrincipal User loggedInUser) {
        try {
            List<ContractResponseDTO> contracts = contractService.getContractsByChat(chatId, loggedInUser);
            return ResponseEntity.ok(contracts);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
