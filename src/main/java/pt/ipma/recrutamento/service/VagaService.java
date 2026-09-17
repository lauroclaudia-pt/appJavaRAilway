package pt.ipma.recrutamento.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pt.ipma.recrutamento.domain.Attachment;
import pt.ipma.recrutamento.domain.Department;
import pt.ipma.recrutamento.domain.Trabalhador;
import pt.ipma.recrutamento.domain.Vaga;
import pt.ipma.recrutamento.domain.WorkLocation;
import pt.ipma.recrutamento.domain.enums.JobState;
import pt.ipma.recrutamento.repository.*;
import pt.ipma.recrutamento.web.dto.VagaCreateRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VagaService {

    private final VagaRepository vagaRepository;
    private final TrabalhadorRepository trabalhadorRepository;
    private final DepartmentRepository departmentRepository;
    private final WorkLocationRepository workLocationRepository;
    private final WorkflowEngineService workflowEngineService;
    private final ApplicantRepository applicantRepository;
    private final AttachmentRepository attachmentRepository;

    @Transactional
    public Vaga create(VagaCreateRequest req) {
        Vaga vaga = new Vaga();
        vaga.setName(req.getName());
        vaga.setOfferType(req.getOfferType());
        vaga.setJobPosition(req.getJobPosition());
        vaga.setNoOfRecruitment(req.getNoOfRecruitment() != null ? req.getNoOfRecruitment() : 1);
        vaga.setPublicationDate(req.getPublicationDate());
        vaga.setBepPublicationDate(req.getBepPublicationDate());
        vaga.setDeadlineDate(req.getDeadlineDate());
        vaga.setVinculo(req.getVinculo());
        vaga.setRegime(req.getRegime());
        if (req.getSalary() != null) vaga.setSalary(req.getSalary());
        if (req.getSalaryPlus() != null) vaga.setSalaryPlus(req.getSalaryPlus());
        vaga.setVacancyRelation(req.getVacancyRelation());
        vaga.setAllowNoDegree(req.isAllowNoDegree());
        vaga.setVagasDeficiencia(req.isVagasDeficiencia());
        vaga.setRequirements(req.getRequirements());
        vaga.setNivelHabilitacional(req.getNivelHabilitacional());
        vaga.setWebsiteDescription(req.getWebsiteDescription());

        // Regras (secção 3.3): PC/AC/Entrevista dependem do tipo de oferta, mas o
        // valor recebido do wizard é respeitado tal como declarado pelo Gestor de RH.
        vaga.setHasPc(req.isHasPc());
        vaga.setHasAc(req.isHasAc());
        vaga.setHasEac(req.isHasEac());

        // Gestor de RH é obrigatório (validado também por @NotNull no DTO).
        Trabalhador gestor = trabalhadorRepository.findById(req.getGestorId())
                .orElseThrow(() -> new IllegalArgumentException("Gestor de RH inválido."));
        vaga.setGestor(gestor);

        if (req.getGestorSuplenteId() != null) vaga.setGestorSuplente(trabalhadorRepository.findById(req.getGestorSuplenteId()).orElse(null));
        if (req.getManagerCdrhId() != null) vaga.setManagerCdrh(trabalhadorRepository.findById(req.getManagerCdrhId()).orElse(null));
        if (req.getJuriPresidenteId() != null) vaga.setJuriPresidente(trabalhadorRepository.findById(req.getJuriPresidenteId()).orElse(null));
        if (req.getJuriVe1Id() != null) vaga.setJuriVogalEfetivo1(trabalhadorRepository.findById(req.getJuriVe1Id()).orElse(null));
        if (req.getJuriVe2Id() != null) vaga.setJuriVogalEfetivo2(trabalhadorRepository.findById(req.getJuriVe2Id()).orElse(null));
        if (req.getJuriVs1Id() != null) vaga.setJuriVogalSuplente1(trabalhadorRepository.findById(req.getJuriVs1Id()).orElse(null));
        if (req.getJuriVs2Id() != null) vaga.setJuriVogalSuplente2(trabalhadorRepository.findById(req.getJuriVs2Id()).orElse(null));

        validateJuriDistinto(vaga);

        if (req.getWorkLocationId() != null) {
            WorkLocation loc = workLocationRepository.findById(req.getWorkLocationId()).orElse(null);
            vaga.setWorkLocation(loc);
        }
        if (req.getDepartmentIds() != null && !req.getDepartmentIds().isEmpty()) {
            Set<Department> departments = new HashSet<>(departmentRepository.findAllById(req.getDepartmentIds()));
            vaga.setDepartments(departments);
        }

        vaga.setState(JobState.DRAFT);

        // Requisitos mínimos para gravar (regra: N.º Vagas, Unidade Orgânica, Cargo, Tipo de Oferta)
        if (vaga.getDepartments().isEmpty()) {
            throw new IllegalArgumentException("É necessário indicar pelo menos uma Unidade Orgânica.");
        }

        workflowEngineService.createPipelineForVaga(vaga);
        return vagaRepository.save(vaga);
    }

    /**
     * O Presidente do Júri, os dois Vogais Efetivos e os dois Vogais Suplentes têm
     * de ser todos distintos entre si (quando preenchidos).
     */
    private void validateJuriDistinto(Vaga vaga) {
        List<Trabalhador> membros = Arrays.asList(
                vaga.getJuriPresidente(), vaga.getJuriVogalEfetivo1(), vaga.getJuriVogalEfetivo2(),
                vaga.getJuriVogalSuplente1(), vaga.getJuriVogalSuplente2());
        List<Long> ids = new ArrayList<>();
        for (Trabalhador t : membros) {
            if (t != null && t.getId() != null) ids.add(t.getId());
        }
        Set<Long> distinct = new HashSet<>(ids);
        if (distinct.size() != ids.size()) {
            throw new IllegalArgumentException(
                    "O Presidente do Júri, os Vogais Efetivos e os Vogais Suplentes têm de ser todos trabalhadores distintos entre si.");
        }
    }

    /** Documento anexado à vaga no momento da abertura (ex.: Edital, Anexos do procedimento). */
    @Transactional
    public void attachDocument(Vaga vaga, String documentType, MultipartFile file, boolean isPublicDocument) throws IOException {
        if (file == null || file.isEmpty()) return;
        Attachment attachment = new Attachment();
        attachment.setFilename(file.getOriginalFilename());
        attachment.setContentType(file.getContentType());
        attachment.setData(file.getBytes());
        attachment.setResModel("vaga");
        attachment.setResId(vaga.getId());
        attachment.setDocumentType(documentType);
        attachment.setPublicDocument(isPublicDocument);
        attachmentRepository.save(attachment);
    }

    /**
     * Publica a vaga no website (secção 4.1): exige Data de Publicação e Código da
     * Oferta BEP / n.º Edital preenchidos, e o Júri identificado.
     */
    @Transactional
    public Vaga publish(Long vagaId, String vagaCode) {
        Vaga vaga = getOrThrow(vagaId);
        if (vagaCode == null || vagaCode.isBlank()) {
            throw new IllegalArgumentException("Código da Oferta BEP / n.º Edital é obrigatório para publicação.");
        }
        if (vagaRepository.existsByVagaCode(vagaCode)) {
            throw new IllegalArgumentException("Já existe uma vaga publicada com este código.");
        }
        if (vaga.getPublicationDate() == null) {
            throw new IllegalArgumentException("Data de Publicação é obrigatória para publicação.");
        }
        if (vaga.getJuriPresidente() == null) {
            throw new IllegalArgumentException("O nome do Presidente do Júri tem de estar preenchido para a vaga ser publicada.");
        }
        vaga.setVagaCode(vagaCode);
        vaga.setWebsitePublished(true);
        vaga.setPublishDate(LocalDateTime.now());
        vaga.setState(JobState.PUBLISHED);
        return vagaRepository.save(vaga);
    }

    public List<Vaga> listPublished() {
        return vagaRepository.findByWebsitePublishedTrueAndDeadlineDateAfter(LocalDateTime.now());
    }

    public List<Vaga> listAll() {
        return vagaRepository.findAll();
    }

    public Vaga getOrThrow(Long id) {
        return vagaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vaga não encontrada: " + id));
    }

    /**
     * Regra 15.3: após a existência da primeira candidatura, o tipo de oferta e o
     * workflow ficam bloqueados contra alterações estruturais.
     */
    public boolean isStructurallyLocked(Long vagaId) {
        return applicantRepository.countByJobId(vagaId) > 0;
    }
}
