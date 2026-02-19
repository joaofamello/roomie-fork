package br.edu.ufape.roomie.service;
import br.edu.ufape.roomie.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TokenServiceTest {
    private TokenService tokenService; 
    private final String SECRET = "test-secret"; 

    @BeforeEach
    void setUp(){
        tokenService = new TokenService(); 
        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
    }

    @Test
    void shouldGenerateToken(){
        User user = new User();
        user.setEmail("test@gmail.com");
        String token = tokenService.generateToken(user); 
        assertNotNull(token); 
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldValidateValidToken() {
        User user = new User(); 
        user.setEmail("test@gmail.com");
        String token = tokenService.generateToken(user); 
        String subject = tokenService.validateToken(token); 
        assertEquals("test@gmail.com", subject);
    }

    @Test 
    void shouldReturnEmptyStringTokenIsInvalid() {
        String invalidToken = "token.invalido"; 
        String result = tokenService.validateToken(invalidToken); 
        assertEquals("", result);
    }
}
