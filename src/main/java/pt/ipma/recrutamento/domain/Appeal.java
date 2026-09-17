package pt.ipma.recrutamento.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pt.ipma.recrutamento.domain.enums.AppealState;
import pt.ipma.recrutamento.domain.enums.Channel;

import java.time.LocalDateTime;

/**
 * hr.appeal — resposta / alegação do candidato durante a Recolha de Requisitos em Falta
 * ou a Audiência de Interessados (secções 7 e 10 da spec). Ao ser criado, o candidato
 * transita para under_appeal, preservando o histórico da exclusão inicial (7.4).
 */
@Entity
@Table(name = "appeal")
@Getter
@Setter
public class Appeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private Applicant applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Vaga job;

    @Column(nullable = false, length = 60)
    private String phase; // missing_requirements | appeal

    @Column(name = "reception_date", nullable = false)
    private LocalDateTime receptionDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Channel channel;

    @Column(columnDefinition = "text")
    private String allegations;

    @Column(name = "rh_response", columnDefinition = "text")
    private String rhResponse;

    @Column(name = "rh_response_date")
    private LocalDateTime rhResponseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppealState state = AppealState.RECEIVED;
}
