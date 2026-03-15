package br.edu.ufape.roomie;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Teste de integração que requer banco de dados completo — executar manualmente")
class RoomieApplicationTests {

  @Test
  void contextLoads() {}
}
