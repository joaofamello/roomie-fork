package br.edu.ufape.roomie.controller;

import br.edu.ufape.roomie.dto.ChatRequestDTO;
import br.edu.ufape.roomie.dto.ChatResponseDTO;
import br.edu.ufape.roomie.dto.MessageRequestDTO;
import br.edu.ufape.roomie.dto.MessageResponseDTO;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.service.ChatService;
import br.edu.ufape.roomie.service.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = ChatController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureJsonTesters
class ChatControllerTest {

    @Autowired private MockMvc mvc;

    @Autowired private JacksonTester<List<ChatResponseDTO>>    chatListTester;
    @Autowired private JacksonTester<List<MessageResponseDTO>> msgListTester;
    @Autowired private JacksonTester<ChatRequestDTO>           reqTester;
    @Autowired private JacksonTester<MessageRequestDTO>        msgReqTester;

    @MockitoBean private ChatService        chatService;
    @MockitoBean private TokenService       tokenService;
    @MockitoBean private UserDetailsService userDetailsService;

    private User mockOwner() {
        User owner = new User();
        owner.setId(1L);
        owner.setEmail("dono@roomie.com");
        return owner;
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );
    }

    private ChatResponseDTO sampleChat() {
        return new ChatResponseDTO(100L, 2L, "Estudante", 1L, "Proprietário", 10L, "Quarto UFAPE",
                LocalDateTime.of(2026, 3, 5, 12, 0), 0L);
    }

    private MessageResponseDTO sampleMessage() {
        return new MessageResponseDTO(200L, 100L, 1L, "Proprietário", "Olá!", LocalDateTime.of(2026, 3, 5, 13, 0), false);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── POST /api/chats ────

    @Test
    @DisplayName("POST /api/chats — 201 quando chat criado com sucesso")
    void deveRetornar201AoCriarChat() throws Exception {
        User owner = mockOwner();
        authenticate(owner);

        ChatRequestDTO req = new ChatRequestDTO();
        req.setStudentId(2L);
        req.setPropertyId(10L);

        when(chatService.startChat(any(ChatRequestDTO.class), any(User.class)))
                .thenReturn(sampleChat());

        var response = mvc.perform(post("/api/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqTester.write(req).getJson()))
                .andReturn().getResponse();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getContentAsString()).contains("\"id\":100");
    }

    @Test
    @DisplayName("POST /api/chats — 403 quando usuário não é dono do imóvel")
    void deveRetornar403QuandoNaoDono() throws Exception {
        User owner = mockOwner();
        authenticate(owner);

        ChatRequestDTO req = new ChatRequestDTO();
        req.setStudentId(2L);
        req.setPropertyId(10L);

        when(chatService.startChat(any(), any()))
                .thenThrow(new IllegalStateException("Apenas o proprietário do imóvel pode iniciar um chat."));

        var response = mvc.perform(post("/api/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqTester.write(req).getJson()))
                .andReturn().getResponse();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentAsString()).isEqualTo("Apenas o proprietário do imóvel pode iniciar um chat.");
    }

    @Test
    @DisplayName("POST /api/chats — 404 quando estudante ou imóvel não encontrado")
    void deveRetornar404QuandoNaoEncontrado() throws Exception {
        User owner = mockOwner();
        authenticate(owner);

        ChatRequestDTO req = new ChatRequestDTO();
        req.setStudentId(999L);
        req.setPropertyId(10L);

        when(chatService.startChat(any(), any()))
                .thenThrow(new RuntimeException("Estudante não encontrado."));

        var response = mvc.perform(post("/api/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqTester.write(req).getJson()))
                .andReturn().getResponse();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getContentAsString()).isEqualTo("Estudante não encontrado.");
    }

    // ── GET /api/chats ───

    @Test
    @DisplayName("GET /api/chats — 200 com lista de chats do usuário")
    void deveRetornar200ComListaDeChats() throws Exception {
        User owner = mockOwner();
        authenticate(owner);

        List<ChatResponseDTO> chats = List.of(sampleChat());
        when(chatService.getChatsForUser(any(User.class))).thenReturn(chats);

        var response = mvc.perform(get("/api/chats"))
                .andReturn().getResponse();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(chatListTester.write(chats).getJson());
    }

    // ── GET /api/chats/{id} ───

    @Test
    @DisplayName("GET /api/chats/{id} — 200 com detalhe do chat")
    void deveRetornar200ComDetalhesDoChat() throws Exception {
        User owner = mockOwner();
        authenticate(owner);

        when(chatService.getChatById(eq(100L), any(User.class))).thenReturn(sampleChat());

        var response = mvc.perform(get("/api/chats/100"))
                .andReturn().getResponse();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).contains("\"id\":100");
    }

    @Test
    @DisplayName("GET /api/chats/{id} — 403 quando usuário não faz parte do chat")
    void deveRetornar403AoBuscarChatDeOutros() throws Exception {
        User owner = mockOwner();
        authenticate(owner);

        when(chatService.getChatById(eq(100L), any(User.class)))
                .thenThrow(new IllegalStateException("Acesso negado: Você não faz parte deste chat."));

        var response = mvc.perform(get("/api/chats/100"))
                .andReturn().getResponse();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("GET /api/chats/{id} — 404 quando chat não encontrado")
    void deveRetornar404QuandoChatNaoEncontrado() throws Exception {
        User owner = mockOwner();
        authenticate(owner);

        when(chatService.getChatById(eq(999L), any(User.class)))
                .thenThrow(new RuntimeException("Chat não encontrado."));

        var response = mvc.perform(get("/api/chats/999"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    // ── GET /api/chats/{id}/messages ───

    @Test
    @DisplayName("GET /api/chats/{id}/messages — 200 com lista de mensagens")
    void deveRetornar200ComMensagens() throws Exception {
        User owner = mockOwner();
        authenticate(owner);

        List<MessageResponseDTO> msgs = List.of(sampleMessage());
        when(chatService.getMessages(eq(100L), any(User.class))).thenReturn(msgs);

        var response = mvc.perform(get("/api/chats/100/messages"))
                .andReturn().getResponse();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(msgListTester.write(msgs).getJson());
    }

    @Test
    @DisplayName("GET /api/chats/{id}/messages — 403 quando acesso negado")
    void deveRetornar403AoBuscarMensagensDeOutros() throws Exception {
        User owner = mockOwner();
        authenticate(owner);

        when(chatService.getMessages(eq(100L), any(User.class)))
                .thenThrow(new IllegalStateException("Acesso negado: Você não faz parte deste chat."));

        var response = mvc.perform(get("/api/chats/100/messages"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    @DisplayName("GET /api/chats/{id}/messages — 404 quando chat não encontrado")
    void deveRetornar404AoBuscarMensagensChatInexistente() throws Exception {
        User owner = mockOwner();
        authenticate(owner);

        when(chatService.getMessages(eq(999L), any(User.class)))
                .thenThrow(new RuntimeException("Chat não encontrado."));

        var response = mvc.perform(get("/api/chats/999/messages"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    // ── POST /api/chats/{id}/messages ───

    @Test
    @DisplayName("POST /api/chats/{id}/messages — 201 ao enviar mensagem com sucesso")
    void deveRetornar201AoEnviarMensagem() throws Exception {
        User owner = mockOwner();
        authenticate(owner);

        MessageRequestDTO req = new MessageRequestDTO();
        req.setContent("Olá!");

        when(chatService.sendMessage(eq(100L), any(MessageRequestDTO.class), any(User.class)))
                .thenReturn(sampleMessage());

        var response = mvc.perform(post("/api/chats/100/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(msgReqTester.write(req).getJson()))
                .andReturn().getResponse();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getContentAsString()).contains("\"content\":\"Olá!\"");
    }

    @Test
    @DisplayName("POST /api/chats/{id}/messages — 400 quando mensagem está vazia")
    void deveRetornar400ComMensagemVazia() throws Exception {
        User owner = mockOwner();
        authenticate(owner);

        MessageRequestDTO req = new MessageRequestDTO();
        req.setContent("   ");

        doThrow(new IllegalArgumentException("O conteúdo da mensagem não pode ser vazio."))
                .when(chatService).sendMessage(eq(100L), any(), any());

        var response = mvc.perform(post("/api/chats/100/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(msgReqTester.write(req).getJson()))
                .andReturn().getResponse();

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo("O conteúdo da mensagem não pode ser vazio.");
    }

    @Test
    @DisplayName("POST /api/chats/{id}/messages — 403 quando usuário não faz parte do chat")
    void deveRetornar403AoEnviarMensagemDeOutros() throws Exception {
        User owner = mockOwner();
        authenticate(owner);

        MessageRequestDTO req = new MessageRequestDTO();
        req.setContent("Olá!");

        doThrow(new IllegalStateException("Acesso negado: Você não faz parte deste chat."))
                .when(chatService).sendMessage(eq(100L), any(), any());

        var response = mvc.perform(post("/api/chats/100/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(msgReqTester.write(req).getJson()))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("POST /api/chats/{id}/messages — 404 quando chat não encontrado")
    void deveRetornar404AoEnviarMensagemChatInexistente() throws Exception {
        User owner = mockOwner();
        authenticate(owner);

        MessageRequestDTO req = new MessageRequestDTO();
        req.setContent("Olá!");

        doThrow(new RuntimeException("Chat não encontrado."))
                .when(chatService).sendMessage(eq(999L), any(), any());

        var response = mvc.perform(post("/api/chats/999/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(msgReqTester.write(req).getJson()))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}



