package pt.ipma.recrutamento.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pt.ipma.recrutamento.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Trabalhador — uma pessoa (funcionário do IPMA ou elemento externo, ex.: membro
 * de júri convidado). O login (email/password) é OPCIONAL. A cada trabalhador
 * podem ser associadas uma ou mais {@link Responsabilidade}, CADA UMA com a sua
 * própria janela de validade (ex.: Gestor de RH de Jan-Jun, Júri de Mar-Dez).
 *
 * O estado geral do trabalhador (Ativo/Inativo) é calculado a partir da sua
 * própria janela (startDate/endDate) — ver {@link #isActive()}.
 */
@Entity
@Table(name = "trabalhador")
@Getter
@Setter
public class Trabalhador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Nulo quando o trabalhador não tem acesso de login (ex.: júri sem conta). */
    @Column(unique = true)
    private String email;

    @Column(name = "password_hash")
    @JsonIgnore
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "trabalhador_responsabilidade", joinColumns = @JoinColumn(name = "trabalhador_id"))
    private Set<Responsabilidade> responsabilidades = new HashSet<>();

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate = LocalDateTime.now();

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    /** Tem esta responsabilidade ATUALMENTE ativa (dentro da janela própria dela). */
    public boolean hasResponsabilidade(Role role) {
        return responsabilidades.stream().anyMatch(r -> r.getRole() == role && r.isActive());
    }

    /** Todas as responsabilidades atualmente ativas (para autorizações e exibição). */
    public Set<Role> activeRoles() {
        Set<Role> roles = new HashSet<>();
        for (Responsabilidade r : responsabilidades) {
            if (r.isActive()) roles.add(r.getRole());
        }
        return roles;
    }

    public boolean hasLogin() {
        return email != null && !email.isBlank();
    }

    /** Ativo: já começou (startDate ≤ agora) e (sem data de fim, ou data de fim ainda não atingida). */
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        boolean started = startDate == null || !startDate.isAfter(now);
        boolean notEnded = endDate == null || endDate.isAfter(now);
        return started && notEnded;
    }
}
