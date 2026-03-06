package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.ChatRequestDTO;
import br.edu.ufape.roomie.dto.ChatResponseDTO;
import br.edu.ufape.roomie.dto.MessageRequestDTO;
import br.edu.ufape.roomie.dto.MessageResponseDTO;
import br.edu.ufape.roomie.model.*;
import br.edu.ufape.roomie.repository.*;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private ChatRepository     chatRepository;
    @Mock private MessageRepository  messageRepository;
    @Mock private StudentRepository  studentRepository;
    @Mock private PropertyRepository propertyRepository;

    @InjectMocks
    private ChatService chatService;

    private User    owner;
    private User    notOwner;
    private Student student;
    private Property property;
    private Chat    chat;
    private Message message;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Proprietário");
        owner.setEmail("dono@roomie.com");

        notOwner = new User();
        notOwner.setId(99L);
        notOwner.setName("Outro Usuário");

        student = new Student();
        student.setId(2L);
        student.setName("Estudante");
        student.setMajor("Engenharia de Software");
        student.setInstitution("UFAPE");

        property = new Property();
        property.setId(10L);
        property.setTitle("Quarto perto da UFAPE");
        property.setOwner(owner);

        chat = new Chat();
        chat.setId(100L);
        chat.setUser(owner);
        chat.setStudent(student);
        chat.setProperty(property);
        chat.setTimestamp(LocalDateTime.now());

        message = new Message();
        message.setId(200L);
        message.setChat(chat);
        message.setSender(owner);
        message.setContent("Olá, tudo bem?");
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
    }

    // ── startChat ───

    @Test
    @DisplayName("Deve criar um novo chat quando não existir entre as partes")
    void deveCriarNovoChatComSucesso() {
        ChatRequestDTO req = new ChatRequestDTO();
        req.setStudentId(2L);
        req.setPropertyId(10L);

        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(chatRepository.findByStudentIdAndUserIdAndPropertyId(2L, 1L, 10L))
                .thenReturn(Optional.empty());
        when(chatRepository.save(any(Chat.class))).thenReturn(chat);
        when(messageRepository.countByChatIdAndReadFalseAndSenderIdNot(anyLong(), anyLong()))
                .thenReturn(0L);

        ChatResponseDTO result = chatService.startChat(req, owner);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStudentName()).isEqualTo("Estudante");
        verify(chatRepository, times(1)).save(any(Chat.class));
    }

    @Test
    @DisplayName("Deve retornar o chat existente quando já houver conversa entre as partes")
    void deveRetornarChatExistenteSeJaExistir() {
        ChatRequestDTO req = new ChatRequestDTO();
        req.setStudentId(2L);
        req.setPropertyId(10L);

        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(chatRepository.findByStudentIdAndUserIdAndPropertyId(2L, 1L, 10L))
                .thenReturn(Optional.of(chat));
        when(messageRepository.countByChatIdAndReadFalseAndSenderIdNot(anyLong(), anyLong()))
                .thenReturn(0L);

        ChatResponseDTO result = chatService.startChat(req, owner);

        assertThat(result.getId()).isEqualTo(100L);
        verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao iniciar chat se o usuário não for o dono do imóvel")
    void deveLancarExcecaoSeNaoForDono() {
        ChatRequestDTO req = new ChatRequestDTO();
        req.setStudentId(2L);
        req.setPropertyId(10L);

        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));

        assertThatThrownBy(() -> chatService.startChat(req, notOwner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Apenas o proprietário do imóvel pode iniciar um chat.");

        verify(chatRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao iniciar chat com estudante inexistente")
    void deveLancarExcecaoComEstudanteInexistente() {
        ChatRequestDTO req = new ChatRequestDTO();
        req.setStudentId(999L);
        req.setPropertyId(10L);

        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.startChat(req, owner))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Estudante não encontrado.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao iniciar chat com imóvel inexistente")
    void deveLancarExcecaoComImovelInexistente() {
        ChatRequestDTO req = new ChatRequestDTO();
        req.setStudentId(2L);
        req.setPropertyId(999L);

        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(propertyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.startChat(req, owner))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Imóvel não encontrado.");
    }

    // ── getChatsForUser ───

    @Test
    @DisplayName("Deve listar os chats do proprietário corretamente")
    void deveListarChatsDoPropietario() {
        when(chatRepository.findByUserId(owner.getId())).thenReturn(List.of(chat));
        when(chatRepository.findByStudentId(owner.getId())).thenReturn(List.of());
        when(messageRepository.countByChatIdAndReadFalseAndSenderIdNot(anyLong(), anyLong()))
                .thenReturn(2L);

        List<ChatResponseDTO> result = chatService.getChatsForUser(owner);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getOwnerName()).isEqualTo("Proprietário");
        assertThat(result.getFirst().getUnreadCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve listar os chats do estudante corretamente")
    void deveListarChatsDosEstudante() {
        when(chatRepository.findByUserId(student.getId())).thenReturn(List.of());
        when(chatRepository.findByStudentId(student.getId())).thenReturn(List.of(chat));
        when(messageRepository.countByChatIdAndReadFalseAndSenderIdNot(anyLong(), anyLong()))
                .thenReturn(0L);

        List<ChatResponseDTO> result = chatService.getChatsForUser(student);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStudentName()).isEqualTo("Estudante");
    }

    // ── getChatById ────

    @Test
    @DisplayName("Deve retornar o chat quando o proprietário solicitar")
    void deveRetornarChatParaProprietario() {
        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));
        when(messageRepository.countByChatIdAndReadFalseAndSenderIdNot(anyLong(), anyLong()))
                .thenReturn(0L);

        ChatResponseDTO result = chatService.getChatById(100L, owner);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Deve retornar o chat quando o estudante solicitar")
    void deveRetornarChatParaEstudante() {
        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));
        when(messageRepository.countByChatIdAndReadFalseAndSenderIdNot(anyLong(), anyLong()))
                .thenReturn(0L);

        ChatResponseDTO result = chatService.getChatById(100L, student);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao acessar chat que não pertence ao usuário")
    void deveLancarExcecaoAcessoChatNegado() {
        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> chatService.getChatById(100L, notOwner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Acesso negado: Você não faz parte deste chat.");
    }

    @Test
    @DisplayName("Deve lançar exceção quando o chat não for encontrado")
    void deveLancarExcecaoChatNaoEncontrado() {
        when(chatRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getChatById(999L, owner))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Chat não encontrado.");
    }

    // ── getMessages ───

    @Test
    @DisplayName("Deve retornar as mensagens e marcar as não lidas como lidas")
    void deveRetornarMensagensEMarcarComoLidas() {
        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));
        when(messageRepository.findByChatIdOrderByTimestampAsc(100L)).thenReturn(List.of(message));

        List<MessageResponseDTO> result = chatService.getMessages(100L, owner);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getContent()).isEqualTo("Olá, tudo bem?");
        verify(messageRepository, times(1))
                .markMessagesAsReadByChatIdAndNotSenderId(100L, owner.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar mensagens de chat de terceiros")
    void deveLancarExcecaoAoBuscarMensagensDeOutros() {
        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> chatService.getMessages(100L, notOwner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Acesso negado: Você não faz parte deste chat.");
    }

    // ── sendMessage ───

    @Test
    @DisplayName("Deve enviar uma mensagem com sucesso")
    void deveEnviarMensagemComSucesso() {
        MessageRequestDTO req = new MessageRequestDTO();
        req.setContent("Olá!");

        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        MessageResponseDTO result = chatService.sendMessage(100L, req, owner);

        assertThat(result).isNotNull();
        assertThat(result.getSenderName()).isEqualTo("Proprietário");
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao enviar mensagem com conteúdo vazio")
    void deveLancarExcecaoComMensagemVazia() {
        MessageRequestDTO req = new MessageRequestDTO();
        req.setContent("   ");

        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> chatService.sendMessage(100L, req, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O conteúdo da mensagem não pode ser vazio.");

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao enviar mensagem em chat de terceiros")
    void deveLancarExcecaoEnviarMensagemAcessoNegado() {
        MessageRequestDTO req = new MessageRequestDTO();
        req.setContent("Olá!");

        when(chatRepository.findById(100L)).thenReturn(Optional.of(chat));

        assertThatThrownBy(() -> chatService.sendMessage(100L, req, notOwner))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Acesso negado: Você não faz parte deste chat.");
    }
}

