package pt.ipma.recrutamento.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pt.ipma.recrutamento.domain.enums.JobState;
import pt.ipma.recrutamento.domain.enums.OfferType;
import pt.ipma.recrutamento.domain.enums.StageCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** hr.job — a Vaga / posto de trabalho a recrutar (secções 3 e 14.1 da spec). */
@Entity
@Table(name = "vaga")
@Getter
@Setter
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "vaga_code", unique = true)
    private String vagaCode; // Código da Oferta BEP / n.º Edital — obrigatório para publicação (DL 121/2008)

    @Enumerated(EnumType.STRING)
    @Column(name = "offer_type", nullable = false, length = 60)
    private OfferType offerType;

    @Column(name = "job_position", nullable = false, length = 120)
    private String jobPosition; // Cargo / Carreira - Categoria

    @Column(name = "no_of_recruitment", nullable = false)
    private Integer noOfRecruitment = 1;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "deadline_date")
    private LocalDateTime deadlineDate;

    private String vinculo;
    private String regime;

    @Column(precision = 12, scale = 2)
    private BigDecimal salary = BigDecimal.ZERO;

    @Column(name = "salary_info", length = 500)
    private String salaryInfo;

    @Column(name = "salary_plus", precision = 12, scale = 2)
    private BigDecimal salaryPlus = BigDecimal.ZERO;

    @Column(name = "vacancy_relation", length = 120)
    private String vacancyRelation;

    @Column(name = "allow_no_degree", nullable = false)
    private boolean allowNoDegree = false;

    @Column(name = "vagas_deficiencia", nullable = false)
    private boolean vagasDeficiencia = false;

    @Column(columnDefinition = "text")
    private String requirements;

    @Column(name = "nationality_required", nullable = false)
    private boolean nationalityRequired = true;

    @Column(name = "nivel_habilitacional", length = 60)
    private String nivelHabilitacional;

    @Column(name = "descricao_habilitacao", columnDefinition = "text")
    private String descricaoHabilitacao;

    @Column(name = "other_requirements", columnDefinition = "text")
    private String otherRequirements;

    @Column(name = "website_description", columnDefinition = "text")
    private String websiteDescription; // Caracterização do Posto de Trabalho

    @Column(name = "procedure_description", columnDefinition = "text")
    private String procedureDescription;

    @Column(name = "docs_list", columnDefinition = "text")
    private String docsList; // Bibliografia / legislação para Prova de Conhecimentos

    @Column(name = "has_pc", nullable = false)
    private boolean hasPc = false;

    @Column(name = "has_ac", nullable = false)
    private boolean hasAc = false;

    @Column(name = "has_eac", nullable = false)
    private boolean hasEac = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestor_id")
    private AppUser gestor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestor_suplente_id")
    private AppUser gestorSuplente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_cdrh_id")
    private AppUser managerCdrh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juri_pres_id")
    private AppUser juriPresidente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juri_ve1_id")
    private AppUser juriVogalEfetivo1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juri_ve2_id")
    private AppUser juriVogalEfetivo2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juri_vs1_id")
    private AppUser juriVogalSuplente1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juri_vs2_id")
    private AppUser juriVogalSuplente2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_location_id")
    private WorkLocation workLocation;

    @ManyToMany
    @JoinTable(name = "vaga_department",
            joinColumns = @JoinColumn(name = "vaga_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id"))
    private Set<Department> departments = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_template_id")
    private WorkflowTemplate workflowTemplate;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage_code", length = 40)
    private StageCode currentStageCode;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<JobStage> stages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private JobState state = JobState.DRAFT;

    @Column(name = "website_published", nullable = false)
    private boolean websitePublished = false;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    @Column(name = "write_date", nullable = false)
    private LocalDateTime writeDate = LocalDateTime.now();

    /** Regra 4.1: só é visível no website se vagaCode preenchido + publicado + dentro do prazo. */
    public boolean isVisibleOnWebsite() {
        return websitePublished
                && vagaCode != null && !vagaCode.isBlank()
                && publicationDate != null && !publicationDate.isAfter(LocalDate.now())
                && deadlineDate != null && deadlineDate.isAfter(LocalDateTime.now());
    }
}
