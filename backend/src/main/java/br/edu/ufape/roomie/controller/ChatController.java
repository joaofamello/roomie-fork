package br.edu.ufape.roomie.controller;

import br.edu.ufape.roomie.dto.ChatRequestDTO;
import br.edu.ufape.roomie.dto.ChatResponseDTO;
import br.edu.ufape.roomie.dto.MessageRequestDTO;
import br.edu.ufape.roomie.dto.MessageResponseDTO;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Inicia um chat (apenas proprietário).
     * POST /api/chats
     */
    @PostMapping
    public ResponseEntity<?> startChat(
            @RequestBody ChatRequestDTO request,
            @AuthenticationPrincipal User loggedInUser) {
        try {
            ChatResponseDTO chat = chatService.startChat(request, loggedInUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(chat);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Lista todos os chats do usuário logado.
     * GET /api/chats
     */
    @GetMapping
    public ResponseEntity<List<ChatResponseDTO>> getMyChats(
            @AuthenticationPrincipal User loggedInUser) {
        return ResponseEntity.ok(chatService.getChatsForUser(loggedInUser));
    }

    /**
     * Retorna detalhes de um chat.
     * GET /api/chats/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getChat(
            @PathVariable Long id,
            @AuthenticationPrincipal User loggedInUser) {
        try {
            return ResponseEntity.ok(chatService.getChatById(id, loggedInUser));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Retorna as mensagens de um chat e marca como lidas.
     * GET /api/chats/{id}/messages
     */
    @GetMapping("/{id}/messages")
    public ResponseEntity<?> getMessages(
            @PathVariable Long id,
            @AuthenticationPrincipal User loggedInUser) {
        try {
            List<MessageResponseDTO> messages = chatService.getMessages(id, loggedInUser);
            return ResponseEntity.ok(messages);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Envia uma mensagem em um chat.
     * POST /api/chats/{id}/messages
     */
    @PostMapping("/{id}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long id,
            @RequestBody MessageRequestDTO request,
            @AuthenticationPrincipal User loggedInUser) {
        try {
            MessageResponseDTO message = chatService.sendMessage(id, request, loggedInUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

