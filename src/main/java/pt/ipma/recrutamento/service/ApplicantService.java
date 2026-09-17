package pt.ipma.recrutamento.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipma.recrutamento.domain.Applicant;
import pt.ipma.recrutamento.domain.Attachment;
import pt.ipma.recrutamento.domain.Vaga;
import pt.ipma.recrutamento.domain.enums.ApplicantState;
import pt.ipma.recrutamento.domain.enums.OfferType;
import pt.ipma.recrutamento.repository.ApplicantRepository;
import pt.ipma.recrutamento.repository.AttachmentRepository;
import pt.ipma.recrutamento.web.dto.ApplicationSubmitRequest;
import pt.ipma.recrutamento.web.dto.TriagemRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApplicantService {

    private final ApplicantRepository applicantRepository;
    private final AttachmentRepository attachmentRepository;
    private final NifValidationService nifValidationService;
    private final NotificationService notificationService;
    private final VagaService vagaService;

    /**
     * Submissão de candidatura via Portal (secção 5 / 16.1.4).
     * Validações: prazo, NIF (Módulo 11), duplicado (NIF+vaga), maioridade,
     * declaração de veracidade, e declarações condicionais por tipo de oferta.
     */
    @Transactional
    public Applicant submit(Long vagaId, ApplicationSubmitRequest req, Map<String, MultipartFile> files) throws IOException {
        Vaga vaga = vagaService.getOrThrow(vagaId);

        if (!vaga.isVisibleOnWebsite()) {
            throw new IllegalStateException("Esta oferta não está a receber candidaturas (prazo encerrado ou vaga não publicada).");
        }

        nifValidationService.validateOrThrow(req.getIdNif());

        if (applicantRepository.existsByJobIdAndIdNif(vagaId, req.getIdNif())) {
            throw new IllegalStateException("Já existe uma candidatura submetida com este NIF para esta oferta.");
        }

        if (req.getBirthDate().plusYears(18).isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("O candidato tem de ter pelo menos 18 anos de idade à data da submissão.");
        }

        if (Boolean.FALSE.equals(req.getDeclarationTrue())) {
            throw new IllegalArgumentException("Não é possível submeter a candidatura sem confirmar a veracidade dos factos.");
        }

        boolean isMobilidade = vaga.getOfferType() == OfferType.MOBILIDADE_INTERNA
                || vaga.getOfferType() == OfferType.MOBILIDADE_INTERCARREIRAS;
        if (isMobilidade && !Boolean.TRUE.equals(req.getMobDec())) {
            throw new IllegalArgumentException("Para concorrer tem de manifestar o seu interesse formalmente na mobilidade.");
        }

        if (req.isHasDisability() && (files == null || !files.containsKey("attachment_disability"))) {
            throw new IllegalArgumentException("É obrigatório anexar a declaração de incapacidade quando indicado 'Tem incapacidade'.");
        }

        if (req.isPublicEmployment() && (files == null || !files.containsKey("attachment_decservico"))) {
            throw new IllegalArgumentException("É obrigatório anexar a declaração do órgão/serviço quando 'Titular de RJEP' = Sim.");
        }

        Applicant applicant = new Applicant();
        applicant.setJob(vaga);
        applicant.setPartnerName(req.getPartnerName());
        applicant.setBirthDate(req.getBirthDate());
        applicant.setGender(req.getGender());
        applicant.setNationality(req.getNationality());
        applicant.setIdNumber(req.getIdNumber());
        applicant.setIdNif(req.getIdNif());
        applicant.setAddress(req.getAddress());
        applicant.setPostalCode(req.getPostalCode());
        applicant.setLocality(req.getLocality());
        applicant.setMunicipality(req.getMunicipality());
        applicant.setEmailFrom(req.getEmailFrom());
        applicant.setPartnerPhone(req.getPartnerPhone());
        applicant.setPartnerMobile(req.getPartnerMobile());
        applicant.setEducationCourse(req.getEducationCourse());
        applicant.setPostgradInfo(req.getPostgradInfo());
        applicant.setPublicEmployment(req.isPublicEmployment());
        applicant.setEmploymentSituation(req.getEmploymentSituation());
        applicant.setLastEmployer(req.getLastEmployer());
        applicant.setLastActivity(req.getLastActivity());
        applicant.setPerformanceEvaluation(req.getPerformanceEvaluation());
        applicant.setRelevantExperience(req.getRelevantExperience());
        applicant.setOtherExperience(req.getOtherExperience());
        applicant.setAlternativeQualification(req.getAlternativeQualification());
        applicant.setSelectionMethods(req.getSelectionMethods());
        applicant.setHasDisability(req.isHasDisability());
        applicant.setSpecialNeedsDesc(req.getSpecialNeedsDesc());
        applicant.setDeclarationTrue(Boolean.TRUE.equals(req.getDeclarationTrue()));
        applicant.setMobDec(req.getMobDec());
        applicant.setState(ApplicantState.SUBMITTED);
        applicant.setCreateDate(LocalDateTime.now());

        applicant = applicantRepository.save(applicant);

        if (files != null) {
            for (var entry : files.entrySet()) {
                storeAttachment(entry.getKey(), entry.getValue(), applicant.getId());
            }
        }

        notificationService.sendCandidacyConfirmation(applicant);
        return applicant;
    }

    private void storeAttachment(String documentType, MultipartFile file, Long applicantId) throws IOException {
        if (file == null || file.isEmpty()) return;
        Attachment attachment = new Attachment();
        attachment.setFilename(file.getOriginalFilename());
        attachment.setContentType(file.getContentType());
        attachment.setData(file.getBytes());
        attachment.setResModel("applicant");
        attachment.setResId(applicantId);
        attachment.setDocumentType(documentType);
        attachment.setPublicDocument(false); // documentos do candidato nunca são públicos
        attachmentRepository.save(attachment);
    }

    /** Preenchimento da triagem (secção 6.2/6.3): habilitação, vínculo, documentos, experiência. */
    @Transactional
    public Applicant triagem(Long applicantId, TriagemRequest req) {
        Applicant applicant = getOrThrow(applicantId);

        applicant.setHabilitOk(req.getHabilitOk());
        applicant.setVinculoOk(req.getVinculoOk());
        applicant.setDocsOk(req.getDocsOk());
        applicant.setExpOk(req.getExpOk());

        boolean anyFail = Boolean.FALSE.equals(req.getHabilitOk())
                || Boolean.FALSE.equals(req.getVinculoOk())
                || Boolean.FALSE.equals(req.getDocsOk())
                || Boolean.FALSE.equals(req.getExpOk());

        if (anyFail) {
            applicant.setState(ApplicantState.EXCLUDED);
            applicant.setMotivoExclusao(req.getMotivoExclusao() != null
                    ? req.getMotivoExclusao()
                    : "Não cumprimento de requisito de admissão.");
        } else {
            applicant.setState(ApplicantState.ADMITTED);
        }
        applicant.setWriteDate(LocalDateTime.now());
        return applicantRepository.save(applicant);
    }

    /** Botão "Notificar e Publicar" (secção 6.6.1): notifica admitidos e excluídos. */
    @Transactional
    public void notifyAndPublish(Long vagaId, int deadlineDaysForResponse) {
        List<Applicant> applicants = applicantRepository.findByJobId(vagaId);
        for (Applicant a : applicants) {
            if (a.getState() == ApplicantState.EXCLUDED) {
                notificationService.sendMissingRequirementsNotification(a, deadlineDaysForResponse);
                a.setChannel(pt.ipma.recrutamento.domain.enums.Channel.SEM_RESPOSTA);
                applicantRepository.save(a);
            } else if (a.getState() == ApplicantState.ADMITTED) {
                notificationService.sendAdmissionNotification(a);
            }
        }
    }

    public Applicant getOrThrow(Long id) {
        return applicantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidato não encontrado: " + id));
    }

    public List<Applicant> listByJob(Long jobId) {
        return applicantRepository.findByJobId(jobId);
    }
}
