package pt.ipma.recrutamento.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pt.ipma.recrutamento.domain.enums.StageCode;

/** recruitment.workflow.stage — etapa pertencente a um dado modelo de workflow. */
@Entity
@Table(name = "workflow_stage")
@Getter
@Setter
public class WorkflowStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private WorkflowTemplate template;

    @Column(nullable = false)
    private Integer sequence = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_code", nullable = false, length = 40)
    private StageCode stageCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private boolean mandatory = true;

    @Column(nullable = false)
    private boolean conditional = false;
}
