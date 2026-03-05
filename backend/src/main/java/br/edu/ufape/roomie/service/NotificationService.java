package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.Student;
import br.edu.ufape.roomie.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String mailFrom;

    /**
     * Envia e-mail HTML ao proprietário quando um estudante demonstra interesse.
     * Executa de forma assíncrona para não bloquear a requisição.
     */
    @Async
    public void notifyOwnerAboutInterest(User owner, User student, Property property) {
        String subject = "🏠 Novo interesse no seu imóvel – " + property.getTitle();

        String studentInfo = student instanceof Student s
                ? s.getName() + " (" + s.getMajor() + " – " + s.getInstitution() + ")"
                : student.getName();

        String html = """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head><meta charset="UTF-8"/></head>
                <body style="margin:0;padding:0;font-family:'Raleway',Arial,sans-serif;background:#f6f5f7;">
                  <table width="100%%" cellpadding="0" cellspacing="0">
                    <tr><td align="center" style="padding:40px 16px;">
                      <table width="560" cellpadding="0" cellspacing="0"
                             style="background:#fff;border-radius:12px;overflow:hidden;
                                    box-shadow:0 2px 12px rgba(0,0,0,.08);">

                        <!-- Header -->
                        <tr>
                          <td style="background:linear-gradient(135deg,#005fa3,#004d87);
                                     padding:32px 40px;text-align:center;">
                            <h1 style="margin:0;color:#fff;font-size:24px;letter-spacing:1px;">Roomie</h1>
                          </td>
                        </tr>

                        <!-- Body -->
                        <tr>
                          <td style="padding:36px 40px;">
                            <p style="margin:0 0 12px;font-size:16px;color:#1a1a2e;">
                              Olá, <strong>%s</strong>! 👋
                            </p>
                            <p style="margin:0 0 24px;font-size:15px;color:#444;line-height:1.6;">
                              Um estudante demonstrou interesse no seu imóvel
                              <strong style="color:#005fa3;">%s</strong>.
                            </p>

                            <!-- Card do estudante -->
                            <div style="background:#e3f2fd;border-radius:8px;padding:20px 24px;margin-bottom:28px;">
                              <p style="margin:0 0 6px;font-size:13px;color:#607d8b;text-transform:uppercase;
                                         letter-spacing:.5px;">Estudante</p>
                              <p style="margin:0;font-size:16px;font-weight:600;color:#1a1a2e;">%s</p>
                            </div>

                            <p style="margin:0 0 28px;font-size:14px;color:#666;line-height:1.6;">
                              Acesse a plataforma Roomie para ver o perfil completo do candidato,
                              aceitar ou recusar a proposta e iniciar uma conversa.
                            </p>

                            <div style="text-align:center;">
                              <a href="https://roomie-front.onrender.com/meus-imoveis"
                                 style="display:inline-block;background:#1565c0;color:#fff;
                                        text-decoration:none;padding:14px 32px;border-radius:8px;
                                        font-weight:600;font-size:15px;">
                                Ver Candidatos
                              </a>
                            </div>
                          </td>
                        </tr>

                        <!-- Footer -->
                        <tr>
                          <td style="background:#f4f6fb;padding:20px 40px;text-align:center;
                                     font-size:12px;color:#999;">
                            Você recebeu este e-mail porque é proprietário de um imóvel no Roomie.<br/>
                            &copy; 2026 Roomie – UFAPE
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(owner.getName(), property.getTitle(), studentInfo);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(owner.getEmail());
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("E-mail de interesse enviado para {} ({})", owner.getName(), owner.getEmail());
        } catch (MessagingException e) {
            log.error("Falha ao enviar e-mail de interesse para {}: {}", owner.getEmail(), e.getMessage(), e);
        }
    }
}

