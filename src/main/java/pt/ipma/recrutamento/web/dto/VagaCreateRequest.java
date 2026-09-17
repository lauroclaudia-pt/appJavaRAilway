package pt.ipma.recrutamento.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pt.ipma.recrutamento.domain.enums.OfferType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class VagaCreateRequest {
    @NotNull private String name;
    @NotNull private OfferType offerType;
    @NotNull private String jobPosition;
    private Integer noOfRecruitment = 1;
    private LocalDate publicationDate;
    /** Data de publicação específica na Bolsa de Emprego Público (distinta da publicação no website). */
    private LocalDate bepPublicationDate;
    private LocalDateTime deadlineDate;
    private String vinculo;
    private String regime;
    private BigDecimal salary;
    private BigDecimal salaryPlus;
    private String vacancyRelation;
    private boolean allowNoDegree;
    private boolean vagasDeficiencia;
    private String requirements;
    private String nivelHabilitacional;
    private String websiteDescription;
    private boolean hasPc;
    private boolean hasAc;
    private boolean hasEac;

    /** Obrigatório — por omissão, o trabalhador que abre a vaga (definido no frontend). */
    @NotNull private Long gestorId;
    private Long gestorSuplenteId;
    private Long managerCdrhId;
    private Long juriPresidenteId;
    private Long juriVe1Id;
    private Long juriVe2Id;
    private Long juriVs1Id;
    private Long juriVs2Id;
    private Long workLocationId;
    private Set<Long> departmentIds;
}
