package pt.ipma.recrutamento.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pt.ipma.recrutamento.domain.enums.OfferType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** recruitment.workflow.template — Modelo de workflow reutilizável por tipo de procedimento (secção 2.5). */
@Entity
@Table(name = "workflow_template")
@Getter
@Setter
public class WorkflowTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "offer_type", nullable = false, length = 60)
    private OfferType offerType;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<WorkflowStage> stages = new ArrayList<>();
}
