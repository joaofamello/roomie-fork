package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(mailSender);
        ReflectionTestUtils.setField(notificationService, "mailFrom", "noreply@roomie.com.br");
    }

    @Test
    @DisplayName("Deve notificar proprietário sem lançar exceções")
    void shouldNotifyOwnerWithoutThrowing() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        User owner = new User();
        owner.setEmail("proprietario@ufape.edu.br");
        owner.setName("Proprietário Teste");

        Student student = new Student();
        student.setName("João");
        student.setMajor("Engenharia de Software");
        student.setInstitution("UFAPE");

        Property property = new Property();
        property.setTitle("Quarto próximo à UFAPE");

        assertDoesNotThrow(() ->
                notificationService.notifyOwnerAboutInterest(owner, student, property)
        );

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}


