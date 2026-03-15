package br.edu.ufape.roomie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import br.edu.ufape.roomie.dto.NotificationResponseDTO;
import br.edu.ufape.roomie.model.Notification;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.repository.NotificationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private NotificationRepository notificationRepository;

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

    assertDoesNotThrow(
        () -> notificationService.notifyOwnerAboutInterest(owner, student, property));

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
  @DisplayName("Deve marcar todas as notificações como lidas filtrando as já lidas")
  void shouldMarkAllNotificationsAsRead() {
    User user = new User();
    Notification n1 = new Notification(user, "Msg 1");
    n1.setRead(false);

    Notification n2 = new Notification(user, "Msg 2");
    n2.setRead(true); // Esta já está lida, deve ser filtrada

    when(notificationRepository.findByRecipientOrderByCreatedAtDesc(user))
        .thenReturn(List.of(n1, n2));

    notificationService.markAllAsRead(user);

    assertThat(n1.isRead()).isTrue();

    ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
    verify(notificationRepository).saveAll(captor.capture());

    List<Notification> savedList = captor.getValue();
    assertThat(savedList).hasSize(1);
    assertThat(savedList.get(0)).isEqualTo(n1);
  }

  @Test
  @DisplayName("Deve marcar uma notificação como lida quando o usuário for o destinatário")
  void shouldMarkAsReadWhenUserIsRecipient() {
    User user = new User();
    user.setId(1L);

    Notification notification = new Notification(user, "Msg 1");
    notification.setId(10L);

    when(notificationRepository.findById(10L)).thenReturn(java.util.Optional.of(notification));

    notificationService.markAsRead(10L, user);

    assertThat(notification.isRead()).isTrue();
    verify(notificationRepository).save(notification);
  }

  @Test
  @DisplayName("Não deve marcar a notificação como lida se o usuário não for o destinatário")
  void shouldNotMarkAsReadWhenUserIsNotRecipient() {
    User user1 = new User();
    user1.setId(1L);

    User user2 = new User();
    user2.setId(2L);

    Notification notification = new Notification(user1, "Msg 1");
    notification.setId(10L);
    notification.setRead(false);

    when(notificationRepository.findById(10L)).thenReturn(java.util.Optional.of(notification));

    notificationService.markAsRead(10L, user2);

    assertThat(notification.isRead()).isFalse();
    verify(notificationRepository, never()).save(any());
  }

  @Test
  @DisplayName("Não deve fazer nada se a notificação não existir no markAsRead")
  void shouldDoNothingWhenNotificationNotFound() {
    User user = new User();
    user.setId(1L);

    when(notificationRepository.findById(10L)).thenReturn(java.util.Optional.empty());

    notificationService.markAsRead(10L, user);

    verify(notificationRepository, never()).save(any());
  }
}
