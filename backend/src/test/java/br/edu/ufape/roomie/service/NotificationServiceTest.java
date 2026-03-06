package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.NotificationResponseDTO;
import br.edu.ufape.roomie.model.Notification;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    @DisplayName("Deve salvar notificação para o proprietário quando há novo interesse")
    void shouldSaveNotificationForOwnerOnInterest() {
        User owner = new User();
        owner.setEmail("proprietario@ufape.edu.br");
        owner.setName("Proprietário Teste");

        Student student = new Student();
        student.setName("João Silva");

        Property property = new Property();
        property.setTitle("Quarto próximo à UFAPE");

        assertDoesNotThrow(() ->
                notificationService.notifyOwnerAboutInterest(owner, student, property)
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getRecipient()).isEqualTo(owner);
        assertThat(saved.getMessage()).contains("João Silva");
        assertThat(saved.getMessage()).contains("Quarto próximo à UFAPE");
        assertThat(saved.isRead()).isFalse();
    }

    @Test
    @DisplayName("Deve retornar notificações do usuário ordenadas por data")
    void shouldReturnNotificationsForUser() {
        User user = new User();
        user.setEmail("user@test.com");

        Notification n1 = new Notification(user, "Notificação 1");
        Notification n2 = new Notification(user, "Notificação 2");

        when(notificationRepository.findByRecipientOrderByCreatedAtDesc(user))
                .thenReturn(List.of(n1, n2));

        List<NotificationResponseDTO> result = notificationService.getNotificationsForUser(user);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMessage()).isEqualTo("Notificação 1");
    }

    @Test
    @DisplayName("Deve marcar todas as notificações como lidas")
    void shouldMarkAllNotificationsAsRead() {
        User user = new User();
        Notification n1 = new Notification(user, "Msg 1");
        Notification n2 = new Notification(user, "Msg 2");

        when(notificationRepository.findByRecipientOrderByCreatedAtDesc(user))
                .thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead(user);

        assertThat(n1.isRead()).isTrue();
        assertThat(n2.isRead()).isTrue();
        verify(notificationRepository).saveAll(anyList());
    }
}
