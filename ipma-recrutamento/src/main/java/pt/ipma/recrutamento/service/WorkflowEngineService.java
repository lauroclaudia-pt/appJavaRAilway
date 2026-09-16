package pt.ipma.recrutamento.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipma.recrutamento.domain.Applicant;
import pt.ipma.recrutamento.domain.JobStage;
import pt.ipma.recrutamento.domain.Vaga;
import pt.ipma.recrutamento.domain.WorkflowTemplate;
import pt.ipma.recrutamento.domain.enums.ApplicantState;
import pt.ipma.recrutamento.domain.enums.StageCode;
import pt.ipma.recrutamento.domain.enums.StageState;
import pt.ipma.recrutamento.repository.ApplicantRepository;
import pt.ipma.recrutamento.repository.JobStageRepository;
import pt.ipma.recrutamento.repository.WorkflowTemplateRepository;
import pt.ipma.recrutamento.repository.VagaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Motor de Regras do Workflow (secção 2.5 e 15.2 da spec).
 *
 * Regras implementadas:
 *  - Ao criar a vaga: identifica o Workflow Template do offer_type, copia as etapas
 *    do template para job_stage, classifica a primeira etapa não-condicional como ACTIVE
 *    e as restantes como DRAFT (as condicionais desativadas ficam SKIPPED).
 *  - Uma e apenas uma etapa está ACTIVE em cada momento.
 *  - A transição só ocorre quando as condições obrigatórias da etapa anterior estão concluídas.
 *  - "Recolha de Requisitos em Falta" só fica ACTIVE se existir >=1 candidato Excluído.
 *  - Após a 1.ª candidatura, a estrutura do workflow (template) fica bloqueada.
 */
@Service
@RequiredArgsConstructor
public class WorkflowEngineService {

    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final JobStageRepository jobStageRepository;
    private final ApplicantRepository applicantRepository;
    private final VagaRepository vagaRepository;

    @Transactional
    public void createPipelineForVaga(Vaga vaga) {
        WorkflowTemplate template = workflowTemplateRepository
                .findByOfferTypeAndActiveTrue(vaga.getOfferType())
                .orElseThrow(() -> new IllegalStateException(
                        "Não existe Workflow Template ativo para o tipo de oferta " + vaga.getOfferType()));

        vaga.setWorkflowTemplate(template);
        vaga.getStages().clear();

        boolean firstActiveAssigned = false;
        for (var templateStage : template.getStages()) {
            JobStage stage = new JobStage();
            stage.setJob(vaga);
            stage.setStageCode(templateStage.getStageCode());
            stage.setName(templateStage.getName());
            stage.setSequence(templateStage.getSequence());
            stage.setMandatory(templateStage.isMandatory());
            stage.setConditional(templateStage.isConditional());

            boolean enabled = isConditionalStageEnabled(vaga, templateStage.getStageCode(), templateStage.isConditional());

            if (!enabled) {
                stage.setState(StageState.SKIPPED);
            } else if (!firstActiveAssigned) {
                stage.setState(StageState.ACTIVE);
                stage.setStartDate(LocalDateTime.now());
                vaga.setCurrentStageCode(stage.getStageCode());
                firstActiveAssigned = true;
            } else {
                stage.setState(StageState.DRAFT);
            }
            vaga.getStages().add(stage);
        }
    }

    /**
     * Etapas condicionais (secção 3.2 / 2.1.2 / 6.4):
     *  - INTERVIEW só é habilitada se vaga.hasEac = true.
     *  - MISSING_REQUIREMENTS é sempre criada como condicional; a sua ativação real
     *    depende do resultado da triagem (ver {@link #evaluateMissingRequirementsStage}),
     *    por isso aqui é sempre SKIPPED por omissão até à triagem decidir.
     */
    private boolean isConditionalStageEnabled(Vaga vaga, StageCode code, boolean conditional) {
        if (!conditional) return true;
        return switch (code) {
            case INTERVIEW -> vaga.isHasEac();
            case MISSING_REQUIREMENTS -> false; // decidido apenas após a triagem (6.4)
            default -> true;
        };
    }

    /** Avança a vaga da etapa atualmente ACTIVE para a etapa seguinte elegível. */
    @Transactional
    public void advanceStage(Vaga vaga) {
        List<JobStage> stages = jobStageRepository.findByJobIdOrderBySequenceAsc(vaga.getId());
        JobStage current = stages.stream()
                .filter(s -> s.getState() == StageState.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Não existe etapa ativa para a vaga " + vaga.getId()));

        current.setState(StageState.COMPLETED);
        current.setEndDate(LocalDateTime.now());

        stages.stream()
                .filter(s -> s.getSequence() > current.getSequence())
                .filter(s -> s.getState() == StageState.DRAFT)
                .findFirst()
                .ifPresent(next -> {
                    next.setState(StageState.ACTIVE);
                    next.setStartDate(LocalDateTime.now());
                    vaga.setCurrentStageCode(next.getStageCode());
                    jobStageRepository.save(next);
                });

        jobStageRepository.save(current);
        vagaRepository.save(vaga);
    }

    /**
     * Regra 6.3/6.4: chamada pelo botão "Concluir Triagem Provisória".
     * COUNT(candidatos excluídos) > 0  => activa "Recolha de Requisitos em Falta"
     * caso contrário                   => a etapa mantém-se SKIPPED e avança-se directamente.
     */
    @Transactional
    public boolean evaluateMissingRequirementsStage(Vaga vaga) {
        List<Applicant> applicants = applicantRepository.findByJobId(vaga.getId());
        boolean allAnalysed = applicants.stream().allMatch(a ->
                a.getState() == ApplicantState.ADMITTED || a.getState() == ApplicantState.EXCLUDED);
        if (!allAnalysed) {
            throw new IllegalStateException("Ainda existem candidatos sem triagem concluída.");
        }

        long excludedCount = applicants.stream().filter(a -> a.getState() == ApplicantState.EXCLUDED).count();

        JobStage currentActive = jobStageRepository.findByJobIdOrderBySequenceAsc(vaga.getId()).stream()
                .filter(s -> s.getState() == StageState.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Não existe etapa ativa para a vaga " + vaga.getId()));

        JobStage missingReqStage = jobStageRepository
                .findByJobIdAndStageCode(vaga.getId(), StageCode.MISSING_REQUIREMENTS)
                .orElse(null);

        // Conclui a etapa atual ("Verificação de Admitidos") antes de decidir o próximo passo.
        currentActive.setState(StageState.COMPLETED);
        currentActive.setEndDate(LocalDateTime.now());
        jobStageRepository.save(currentActive);

        if (excludedCount > 0 && missingReqStage != null) {
            missingReqStage.setState(StageState.ACTIVE);
            missingReqStage.setStartDate(LocalDateTime.now());
            jobStageRepository.save(missingReqStage);
            vaga.setCurrentStageCode(StageCode.MISSING_REQUIREMENTS);
            vagaRepository.save(vaga);
            return true;
        } else {
            if (missingReqStage != null) {
                missingReqStage.setState(StageState.SKIPPED);
                jobStageRepository.save(missingReqStage);
            }
            // Avança directamente para a etapa seguinte à "Verificação de Admitidos" (ex.: Avaliação).
            jobStageRepository.findByJobIdOrderBySequenceAsc(vaga.getId()).stream()
                    .filter(s -> s.getSequence() > currentActive.getSequence())
                    .filter(s -> s.getState() == StageState.DRAFT)
                    .findFirst()
                    .ifPresent(next -> {
                        next.setState(StageState.ACTIVE);
                        next.setStartDate(LocalDateTime.now());
                        vaga.setCurrentStageCode(next.getStageCode());
                        jobStageRepository.save(next);
                        vagaRepository.save(vaga);
                    });
            return false;
        }
    }
}
