package pt.ipma.recrutamento.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Local de trabalho, geríbel em Administração. Estado calculado — ver {@link #isActive()}. */
@Entity
@Table(name = "work_location")
@Getter
@Setter
public class WorkLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String municipality;

    private String address;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate = LocalDateTime.now();

    @Column(name = "end_date")
    private LocalDateTime endDate;

    /** Ativo: já começou (startDate ≤ agora) e (sem data de fim, ou data de fim ainda não atingida). */
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        boolean started = startDate == null || !startDate.isAfter(now);
        boolean notEnded = endDate == null || endDate.isAfter(now);
        return started && notEnded;
    }
}
