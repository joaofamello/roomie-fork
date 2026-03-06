package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.ChatRequestDTO;
import br.edu.ufape.roomie.dto.ChatResponseDTO;
import br.edu.ufape.roomie.dto.MessageRequestDTO;
import br.edu.ufape.roomie.dto.MessageResponseDTO;
import br.edu.ufape.roomie.model.Chat;
import br.edu.ufape.roomie.model.Message;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.ChatRepository;
import br.edu.ufape.roomie.repository.MessageRepository;
import br.edu.ufape.roomie.repository.PropertyRepository;
import br.edu.ufape.roomie.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final StudentRepository studentRepository;
    private final PropertyRepository propertyRepository;

    /**
     * Inicia um chat entre um proprietário e um estudante para um imóvel.
     * Se já existir um chat entre as partes, retorna o existente.
     */
    @Transactional
    public ChatResponseDTO startChat(ChatRequestDTO request, User owner) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado."));

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));

        if (!property.getOwner().getId().equals(owner.getId())) {
            throw new IllegalStateException("Apenas o proprietário do imóvel pode iniciar um chat.");
        }

        return chatRepository.findByStudentIdAndUserIdAndPropertyId(student.getId(), owner.getId(), property.getId())
                .map(existing -> toDTO(existing, owner))
                .orElseGet(() -> {
                    Chat chat = new Chat();
                    chat.setStudent(student);
                    chat.setUser(owner);
                    chat.setProperty(property);
                    chat.setTimestamp(LocalDateTime.now());
                    Chat saved = chatRepository.save(chat);
                    return toDTO(saved, owner);
                });
    }

    /**
     * Retorna todos os chats do usuário logado (proprietário ou estudante).
     * Busca por ID puro em ambos os papéis.
     */
    @Transactional(readOnly = true)
    public List<ChatResponseDTO> getChatsForUser(User user) {
        // Busca chats onde o usuário é proprietário
        List<Chat> asOwner = chatRepository.findByUserId(user.getId());
        // Busca chats onde o usuário é estudante interessado
        List<Chat> asStudent = chatRepository.findByStudentId(user.getId());

        // Combina e remove duplicatas (caso um usuário seja dono e também tenha demonstrado interesse)
        java.util.Set<Long> seen = new java.util.LinkedHashSet<>();
        java.util.List<Chat> allChats = new java.util.ArrayList<>();
        for (Chat c : asOwner)   { if (seen.add(c.getId())) allChats.add(c); }
        for (Chat c : asStudent) { if (seen.add(c.getId())) allChats.add(c); }

        return allChats.stream()
                .map(chat -> toDTO(chat, user))
                .collect(Collectors.toList());
    }

    /**
     * Busca um chat por ID e valida acesso do usuário.
     */
    @Transactional(readOnly = true)
    public ChatResponseDTO getChatById(Long chatId, User user) {
        Chat chat = findAndValidate(chatId, user);
        return toDTO(chat, user);
    }

    /**
     * Retorna as mensagens de um chat e marca as não lidas como lidas.
     */
    @Transactional
    public List<MessageResponseDTO> getMessages(Long chatId, User user) {
        findAndValidate(chatId, user);
        messageRepository.markMessagesAsReadByChatIdAndNotSenderId(chatId, user.getId());
        return messageRepository.findByChatIdOrderByTimestampAsc(chatId)
                .stream().map(this::toMessageDTO).collect(Collectors.toList());
    }

    /**
     * Envia uma mensagem em um chat.
     */
    @Transactional
    public MessageResponseDTO sendMessage(Long chatId, MessageRequestDTO request, User sender) {
        Chat chat = findAndValidate(chatId, sender);

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("O conteúdo da mensagem não pode ser vazio.");
        }

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(request.getContent().trim());
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);

        return toMessageDTO(messageRepository.save(message));
    }

    // ─── helpers ────

    private Chat findAndValidate(Long chatId, User user) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat não encontrado."));

        boolean isOwner = chat.getUser().getId().equals(user.getId());
        boolean isStudent = chat.getStudent().getId().equals(user.getId());

        if (!isOwner && !isStudent) {
            throw new IllegalStateException("Acesso negado: Você não faz parte deste chat.");
        }
        return chat;
    }

    private ChatResponseDTO toDTO(Chat chat, User currentUser) {
        long unreadCount = messageRepository
                .countByChatIdAndReadFalseAndSenderIdNot(chat.getId(), currentUser.getId());
        return new ChatResponseDTO(
                chat.getId(),
                chat.getStudent().getId(),
                chat.getStudent().getName(),
                chat.getUser().getId(),
                chat.getUser().getName(),
                chat.getProperty().getId(),
                chat.getProperty().getTitle(),
                chat.getTimestamp(),
                unreadCount
        );
    }

    private MessageResponseDTO toMessageDTO(Message message) {
        return new MessageResponseDTO(
                message.getId(),
                message.getChat().getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getContent(),
                message.getTimestamp(),
                message.getRead()
        );
    }
}
