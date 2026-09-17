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
 * de júri convidado). O login (email/password) é OPCIONAL: um trabalhador pode
 * ser indicado como júri sem nunca aceder à plataforma. A cada trabalhador pode
 * ser associada uma ou mais responsabilidades (ADMIN, CDRH, GESTOR_RH, JURI);
 * quando existe login, o acesso reflete a UNIÃO de permissões de todas elas.
 *
 * O estado (Ativo/Inativo) é CALCULADO a partir da janela de validade
 * (startDate/endDate), tal como nas Opções de Candidatura — ver {@link #isActive()}.
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
    @Column(name = "responsabilidade", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private Set<Role> responsabilidades = new HashSet<>();

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate = LocalDateTime.now();

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    public boolean hasResponsabilidade(Role role) {
        return responsabilidades.contains(role);
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
