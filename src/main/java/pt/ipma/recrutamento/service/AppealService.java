package pt.ipma.recrutamento.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipma.recrutamento.domain.Appeal;
import pt.ipma.recrutamento.domain.Applicant;
import pt.ipma.recrutamento.domain.enums.ApplicantState;
import pt.ipma.recrutamento.domain.enums.Channel;
import pt.ipma.recrutamento.repository.AppealRepository;
import pt.ipma.recrutamento.repository.ApplicantRepository;
import pt.ipma.recrutamento.web.dto.AppealSubmitRequest;

/**
 * Recolha de Requisitos em Falta / Audiência de Interessados (secções 7 e 10 da spec).
 *
 * Regra 7.4 (melhoria adotada): ao responder, o candidato NÃO perde o estado de
 * exclusão original — transita para UNDER_APPEAL, preservando o histórico e
 * forçando a reanálise obrigatória pelo Gestor de RH / Júri.
 */
@Service
@RequiredArgsConstructor
public class AppealService {

    private final AppealRepository appealRepository;
    private final ApplicantRepository applicantRepository;

    @Transactional
    public Appeal submitFromPortal(Long applicantId, AppealSubmitRequest req) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado: " + applicantId));

        if (applicant.getState() != ApplicantState.EXCLUDED) {
            throw new IllegalStateException("Só é possível submeter alegação para candidatos no estado Excluído.");
        }

        Appeal appeal = new Appeal();
        appeal.setApplicant(applicant);
        appeal.setJob(applicant.getJob());
        appeal.setPhase(req.getPhase());
        appeal.setChannel(Channel.PORTAL);
        appeal.setAllegations(req.getAllegations());
        appeal = appealRepository.save(appeal);

        // Regra 7.4: preserva o histórico de exclusão, transita para under_appeal.
        applicant.setState(ApplicantState.UNDER_APPEAL);
        applicant.setChannel(Channel.PORTAL);
        applicantRepository.save(applicant);

        return appeal;
    }

    /** Registo manual de resposta recebida por Email ou via documento físico (secção 7.4). */
    @Transactional
    public Appeal submitManual(Long applicantId, String phase, Channel channel, String allegations) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado: " + applicantId));

        Appeal appeal = new Appeal();
        appeal.setApplicant(applicant);
        appeal.setJob(applicant.getJob());
        appeal.setPhase(phase);
        appeal.setChannel(channel);
        appeal.setAllegations(allegations);
        appeal = appealRepository.save(appeal);

        applicant.setState(ApplicantState.UNDER_APPEAL);
        applicant.setChannel(channel);
        applicantRepository.save(applicant);

        return appeal;
    }

    /** Decisão do Júri / Gestor de RH sobre a alegação (secção 7.5). */
    @Transactional
    public Appeal decide(Long appealId, boolean admitido, String rhResponse) {
        Appeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new IllegalArgumentException("Alegação não encontrada: " + appealId));

        appeal.setRhResponse(rhResponse);
        appeal.setRhResponseDate(java.time.LocalDateTime.now());
        appeal.setState(pt.ipma.recrutamento.domain.enums.AppealState.DECIDED);
        appealRepository.save(appeal);

        Applicant applicant = appeal.getApplicant();
        applicant.setState(admitido ? ApplicantState.ADMITTED : ApplicantState.EXCLUDED);
        applicant.setRhResponse(rhResponse);
        applicantRepository.save(applicant);

        return appeal;
    }
}
