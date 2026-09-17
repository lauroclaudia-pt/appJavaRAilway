package pt.ipma.recrutamento.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pt.ipma.recrutamento.domain.Applicant;
import pt.ipma.recrutamento.domain.Vaga;

import java.time.format.DateTimeFormatter;

/**
 * Notificações por email (secção 15.4 da spec). Em ambiente de desenvolvimento
 * (app.mail.enabled=false) os emails são apenas registados em log, para que a
 * plataforma funcione em Railway sem uma conta SMTP configurada.
 */
@Slf4j
@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final boolean mailEnabled;
    private final String portalBaseUrl;

    public NotificationService(JavaMailSender mailSender,
                                @Value("${app.mail.from}") String fromAddress,
                                @Value("${app.mail.enabled}") boolean mailEnabled,
                                @Value("${app.ipma.portal-base-url}") String portalBaseUrl) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.mailEnabled = mailEnabled;
        this.portalBaseUrl = portalBaseUrl;
    }

    /** 15.4.1 — Notificação de Candidatura submetida com sucesso. */
    public void sendCandidacyConfirmation(Applicant applicant) {
        String subject = "IPMA — Confirmação de Candidatura #Ref-" + applicant.getId();
        String body = """
                Exmo.(a) Senhor(a) %s,

                Confirmamos a receção com sucesso da sua candidatura ao procedimento concursal
                público do Instituto Português do Mar e da Atmosfera, I.P. (IPMA):

                Procedimento / Vaga: %s
                Código da Oferta BEP / Edital: %s
                Número de Referência da Candidatura: #Ref-%d
                Data/Hora de Submissão: %s

                A sua candidatura encontra-se atualmente na fase de Verificação de Requisitos
                de Admissão (Triagem). Poderá acompanhar o estado do processo no Portal do
                Candidato: %s/portal/applicants/%d

                Com os melhores cumprimentos,
                Divisão de Recursos Humanos
                Instituto Português do Mar e da Atmosfera, I.P.
                """.formatted(
                applicant.getPartnerName(),
                applicant.getJob().getName(),
                applicant.getJob().getVagaCode(),
                applicant.getId(),
                applicant.getCreateDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                portalBaseUrl, applicant.getId());

        send(applicant.getEmailFrom(), subject, body);
    }

    /** 15.4.2 — Notificação de Exclusão Provisória (Recolha de Requisitos em Falta). */
    public void sendMissingRequirementsNotification(Applicant applicant, int deadlineDays) {
        String subject = "IPMA — Lista Provisória de Admitidos e Excluídos — " + applicant.getJob().getName();
        String body = """
                Exmo.(a) Senhor(a) %s,

                Na sequência do procedimento concursal para %s (Código BEP: %s), informamos
                que o Júri procedeu à verificação dos requisitos de admissão.

                A sua candidatura foi classificada provisoriamente como EXCLUÍDA, pelo motivo:
                %s

                Nos termos do Código do Procedimento Administrativo, dispõe de um prazo de
                %d dias úteis para se pronunciar, contestar a decisão ou suprir os requisitos
                em falta, através do Portal do Candidato: %s/portal/applicants/%d

                Com os melhores cumprimentos,
                O Júri do Procedimento Concursal
                IPMA, I.P.
                """.formatted(
                applicant.getPartnerName(),
                applicant.getJob().getName(),
                applicant.getJob().getVagaCode(),
                applicant.getMotivoExclusao() != null ? applicant.getMotivoExclusao() : "Não apresentação de documentos obrigatórios de admissão.",
                deadlineDays,
                portalBaseUrl, applicant.getId());

        send(applicant.getEmailFrom(), subject, body);
    }

    /** 15.4.3 — Notificação de Admissão Definitiva e convocatória para métodos de seleção. */
    public void sendAdmissionNotification(Applicant applicant) {
        String subject = "IPMA — Admissão Definitiva — " + applicant.getJob().getName();
        String body = """
                Exmo.(a) Senhor(a) %s,

                Informamos que a sua candidatura ao procedimento concursal para %s
                (Código BEP: %s) foi classificada como ADMITIDA DEFINITIVAMENTE.

                Será submetido(a) ao método de seleção aplicável nos termos do artigo 36.º
                da LTFP. Consulte o cronograma detalhado na página do procedimento no
                website público.

                Com os melhores cumprimentos,
                O Júri do Procedimento Concursal
                IPMA, I.P.
                """.formatted(applicant.getPartnerName(), applicant.getJob().getName(), applicant.getJob().getVagaCode());

        send(applicant.getEmailFrom(), subject, body);
    }

    /** Notificação genérica à equipa de Comunicação após publicação da vaga. */
    public void sendJobPublishedNotification(Vaga vaga, String communicationTeamEmail) {
        String subject = "Nova oferta publicada — " + vaga.getName();
        String body = "A oferta '%s' (Código %s) foi publicada no website do IPMA e está disponível para divulgação."
                .formatted(vaga.getName(), vaga.getVagaCode());
        send(communicationTeamEmail, subject, body);
    }

    private void send(String to, String subject, String body) {
        if (!mailEnabled) {
            log.info("[MAIL DESATIVADO] Para: {} | Assunto: {}\n{}", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Falha ao enviar email para {}: {}", to, ex.getMessage());
        }
    }
}
