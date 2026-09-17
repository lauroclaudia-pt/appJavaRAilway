package pt.ipma.recrutamento.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pt.ipma.recrutamento.domain.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** hr.applicant — cada registo representa uma candidatura ao procedimento (secções 5, 6, 14.1). */
@Entity
@Table(name = "applicant", uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "id_nif"}))
@Getter
@Setter
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonIgnore
    private Vaga job;

    // --- Secção A: Identificação do candidato ---
    @Column(name = "partner_name", nullable = false)
    private String partnerName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private String nationality;

    @Column(name = "id_number", nullable = false, length = 60)
    private String idNumber; // CC / BI / Passaporte

    @Column(name = "id_nif", nullable = false, length = 9)
    private String idNif; // NIF — validado via Módulo 11

    @Column(length = 500)
    private String address;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    private String locality;
    private String municipality;

    @Column(name = "email_from", nullable = false)
    private String emailFrom;

    @Column(name = "partner_phone", length = 30)
    private String partnerPhone;

    @Column(name = "partner_mobile", nullable = false, length = 30)
    private String partnerMobile;

    // --- Secção B: Situação Habilitacional e Profissional ---
    @Column(name = "education_course", columnDefinition = "text")
    private String educationCourse;

    @Column(name = "postgrad_info", columnDefinition = "text")
    private String postgradInfo;

    @Column(name = "public_employment", nullable = false)
    private boolean publicEmployment = false;

    @Column(name = "employment_situation")
    private String employmentSituation;

    @Column(name = "last_employer")
    private String lastEmployer;

    @Column(name = "last_activity")
    private String lastActivity;

    @Column(name = "performance_evaluation")
    private String performanceEvaluation;

    @Column(name = "relevant_experience", columnDefinition = "text")
    private String relevantExperience;

    @Column(name = "other_experience", columnDefinition = "text")
    private String otherExperience;

    @Column(name = "alternative_qualification", columnDefinition = "text")
    private String alternativeQualification;

    // --- Secção C/D: Método de seleção e condições especiais ---
    @Enumerated(EnumType.STRING)
    @Column(name = "selection_methods", length = 20)
    private SelectionMethod selectionMethods;

    @Column(name = "has_disability", nullable = false)
    private boolean hasDisability = false;

    @Column(name = "special_needs_desc", columnDefinition = "text")
    private String specialNeedsDesc;

    // --- Secção F: Declarações ---
    @Column(name = "declaration_true", nullable = false)
    private boolean declarationTrue = false;

    @Column(name = "mob_dec")
    private Boolean mobDec;

    // --- Triagem (Requisitos de Admissão — secção 6) ---
    @Column(name = "habilit_ok")
    private Boolean habilitOk;

    @Column(name = "vinculo_ok")
    private Boolean vinculoOk;

    @Column(name = "docs_ok")
    private Boolean docsOk;

    @Column(name = "exp_ok")
    private Boolean expOk;

    @Column(name = "motivo_exclusao", columnDefinition = "text")
    private String motivoExclusao;

    @Column(name = "rh_response", columnDefinition = "text")
    private String rhResponse;

    // --- Avaliação ---
    @Column(name = "pc_grade", precision = 5, scale = 3)
    private BigDecimal pcGrade;

    @Column(name = "ac_grade", precision = 5, scale = 3)
    private BigDecimal acGrade;

    @Column(name = "eac_grade", precision = 5, scale = 3)
    private BigDecimal eacGrade;

    @Column(name = "final_grade", precision = 5, scale = 3)
    private BigDecimal finalGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicantState state = ApplicantState.SUBMITTED;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Channel channel;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    @Column(name = "write_date", nullable = false)
    private LocalDateTime writeDate = LocalDateTime.now();
}
