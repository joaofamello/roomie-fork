package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.enums.UserRole;
import br.edu.ufape.roomie.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TokenServiceTest {
    private final String SECRET = "test-secret";
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
    }

    @Test
    @DisplayName("Deve gerar um token JWT não nulo e não vazio para um usuário válido")
    void shouldGenerateToken() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setRole(UserRole.USER);
        String token = tokenService.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Deve validar o token e extrair o email (subject) corretamente")
    void shouldValidateValidToken() {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setRole(UserRole.USER);
        String token = tokenService.generateToken(user);
        String subject = tokenService.validateToken(token);
        assertEquals("test@gmail.com", subject);
    }

    @Test
    @DisplayName("Deve retornar uma string vazia ao tentar validar um token malformado ou inválido")
    void shouldReturnEmptyStringTokenIsInvalid() {
        String invalidToken = "token.invalido";
        String result = tokenService.validateToken(invalidToken);
        assertEquals("", result);
    }
}