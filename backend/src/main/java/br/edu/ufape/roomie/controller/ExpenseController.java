package br.edu.ufape.roomie.controller;

import br.edu.ufape.roomie.dto.ExpenseRequestDTO;
import br.edu.ufape.roomie.dto.ExpenseResponseDTO;
import br.edu.ufape.roomie.dto.ExpenseSummaryDTO;
import br.edu.ufape.roomie.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> createExpense(@Valid @RequestBody ExpenseRequestDTO dto) {
        ExpenseResponseDTO created = expenseService.createExpense(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<ExpenseSummaryDTO> getExpensesByProperty(@PathVariable Long propertyId) {
        return ResponseEntity.ok(expenseService.getExpensesByProperty(propertyId));
    }
}