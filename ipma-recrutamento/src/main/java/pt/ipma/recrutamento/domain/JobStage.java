package pt.ipma.recrutamento.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pt.ipma.recrutamento.domain.enums.StageCode;
import pt.ipma.recrutamento.domain.enums.StageState;

import java.time.LocalDateTime;

/**
 * recruitment.job.stage — Instância de etapa do workflow de UMA vaga concreta.
 * Copiada do WorkflowTemplate/WorkflowStage na criação da vaga; durante a execução
 * do procedimento não são criadas nem removidas etapas, apenas transitado o estado (secção 2.5).
 */
@Entity
@Table(name = "job_stage", uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "stage_code"}))
@Getter
@Setter
public class JobStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Vaga job;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_code", nullable = false, length = 40)
    private StageCode stageCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private Integer sequence = 10;

    @Column(nullable = false)
    private boolean mandatory = true;

    @Column(nullable = false)
    private boolean conditional = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StageState state = StageState.DRAFT;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * Transições permitidas (secção 15.2 / regras de negócio):
     *   draft -> active
     *   active -> completed | skipped | cancelled
     * Nunca: draft -> completed, completed -> active, cancelled -> active (exceto ADMIN).
     */
    public boolean canTransitionTo(StageState target) {
        return switch (this.state) {
            case DRAFT -> target == StageState.ACTIVE || target == StageState.SKIPPED;
            case ACTIVE -> target == StageState.COMPLETED || target == StageState.SKIPPED || target == StageState.CANCELLED;
            default -> false;
        };
    }
}
