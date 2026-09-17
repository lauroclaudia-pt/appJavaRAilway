package pt.ipma.recrutamento.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ipma.recrutamento.domain.Department;
import pt.ipma.recrutamento.domain.Vaga;
import pt.ipma.recrutamento.domain.WorkLocation;
import pt.ipma.recrutamento.domain.enums.JobState;
import pt.ipma.recrutamento.repository.*;
import pt.ipma.recrutamento.web.dto.VagaCreateRequest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VagaService {

    private final VagaRepository vagaRepository;
    private final AppUserRepository appUserRepository;
    private final DepartmentRepository departmentRepository;
    private final WorkLocationRepository workLocationRepository;
    private final WorkflowEngineService workflowEngineService;
    private final ApplicantRepository applicantRepository;

    @Transactional
    public Vaga create(VagaCreateRequest req) {
        Vaga vaga = new Vaga();
        vaga.setName(req.getName());
        vaga.setOfferType(req.getOfferType());
        vaga.setJobPosition(req.getJobPosition());
        vaga.setNoOfRecruitment(req.getNoOfRecruitment() != null ? req.getNoOfRecruitment() : 1);
        vaga.setPublicationDate(req.getPublicationDate());
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

        if (req.getGestorId() != null) vaga.setGestor(appUserRepository.findById(req.getGestorId()).orElse(null));
        if (req.getGestorSuplenteId() != null) vaga.setGestorSuplente(appUserRepository.findById(req.getGestorSuplenteId()).orElse(null));
        if (req.getManagerCdrhId() != null) vaga.setManagerCdrh(appUserRepository.findById(req.getManagerCdrhId()).orElse(null));
        if (req.getJuriPresidenteId() != null) vaga.setJuriPresidente(appUserRepository.findById(req.getJuriPresidenteId()).orElse(null));
        if (req.getJuriVe1Id() != null) vaga.setJuriVogalEfetivo1(appUserRepository.findById(req.getJuriVe1Id()).orElse(null));
        if (req.getJuriVe2Id() != null) vaga.setJuriVogalEfetivo2(appUserRepository.findById(req.getJuriVe2Id()).orElse(null));
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
