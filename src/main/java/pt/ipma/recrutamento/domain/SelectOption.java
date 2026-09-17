package pt.ipma.recrutamento.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pt.ipma.recrutamento.domain.enums.OptionCategory;

import java.time.LocalDateTime;

/**
 * Opção de uma lista configurável pela Administração (ex.: Vínculo, Regime,
 * Nível Habilitacional). Cada opção tem uma janela de validade: só é considerada
 * "ativa" (visível nos formulários) enquanto startDate já passou e endDate é nulo
 * ou ainda não foi atingido — ver {@link #isActive()}.
 */
@Entity
@Table(name = "select_option", uniqueConstraints = @UniqueConstraint(columnNames = {"category", "value"}))
@Getter
@Setter
public class SelectOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OptionCategory category;

    /** Código interno guardado nos registos (ex.: "LICENCIATURA"). Estável mesmo que o rótulo mude. */
    @Column(nullable = false, length = 100)
    private String value;

    /** Texto apresentado ao utilizador (ex.: "Licenciatura"). */
    @Column(nullable = false, length = 255)
    private String label;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate = LocalDateTime.now();

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /** Visível/ativa: já começou e (não tem fim OU o fim ainda não foi atingido). */
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        boolean started = startDate == null || !startDate.isAfter(now);
        boolean notEnded = endDate == null || endDate.isAfter(now);
        return started && notEnded;
    }
}
