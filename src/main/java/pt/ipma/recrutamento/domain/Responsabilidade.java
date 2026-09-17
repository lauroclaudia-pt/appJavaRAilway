package pt.ipma.recrutamento.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pt.ipma.recrutamento.domain.enums.Role;

import java.time.LocalDate;

/**
 * Uma responsabilidade atribuída a um trabalhador (ex.: JURI), com a sua PRÓPRIA
 * janela de validade — a mesma pessoa pode ser Gestor de RH de Janeiro a Junho e
 * Júri de Março a Dezembro, em simultâneo. As datas são do tipo DATE (sem hora).
 */
@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class Responsabilidade {

    @Enumerated(EnumType.STRING)
    @Column(name = "responsabilidade", nullable = false, length = 40)
    private Role role;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate = LocalDate.now();

    @Column(name = "end_date")
    private LocalDate endDate;

    public Responsabilidade(Role role, LocalDate startDate, LocalDate endDate) {
        this.role = role;
        this.startDate = startDate != null ? startDate : LocalDate.now();
        this.endDate = endDate;
    }

    /** Ativa: data de início ≤ hoje e (sem data de fim, ou data de fim ≥ hoje). */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        boolean started = startDate == null || !startDate.isAfter(today);
        boolean notEnded = endDate == null || !endDate.isBefore(today);
        return started && notEnded;
    }
}
