package pt.ipma.recrutamento.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pt.ipma.recrutamento.domain.Responsabilidade;
import pt.ipma.recrutamento.domain.Trabalhador;
import pt.ipma.recrutamento.domain.enums.Role;
import pt.ipma.recrutamento.repository.TrabalhadorRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Administração de trabalhadores. Cada responsabilidade (ADMIN, CDRH, GESTOR_RH,
 * JURI) tem a SUA PRÓPRIA janela de validade (startDate/endDate, tipo DATE) — a
 * mesma pessoa pode ser Gestor de RH num período e Júri noutro, em simultâneo ou
 * não. O login (email/password) é opcional. Acesso restrito a ADMIN.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final TrabalhadorRepository trabalhadorRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public List<Trabalhador> list() {
        return trabalhadorRepository.findAll();
    }

    @PostMapping
    public Trabalhador create(@RequestBody UserRequest req) {
        boolean wantsLogin = req.getEmail() != null && !req.getEmail().isBlank();

        if (wantsLogin && trabalhadorRepository.findByEmailIgnoreCase(req.getEmail()).isPresent()) {
            throw new IllegalStateException("Já existe um trabalhador com este email.");
        }
        if (wantsLogin && (req.getPassword() == null || req.getPassword().isBlank())) {
            throw new IllegalArgumentException("É necessário definir uma palavra-passe quando é indicado um email de acesso.");
        }

        Trabalhador trabalhador = new Trabalhador();
        trabalhador.setName(req.getName());
        if (wantsLogin) {
            trabalhador.setEmail(req.getEmail());
            trabalhador.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        trabalhador.setResponsabilidades(toResponsabilidades(req.getResponsabilidades()));
        trabalhador.setStartDate(req.getStartDate() != null ? req.getStartDate() : LocalDateTime.now());
        trabalhador.setEndDate(req.getEndDate());
        return trabalhadorRepository.save(trabalhador);
    }

    @PutMapping("/{id}")
    public Trabalhador update(@PathVariable Long id, @RequestBody UserRequest req) {
        Trabalhador trabalhador = trabalhadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trabalhador não encontrado: " + id));

        boolean wantsLogin = req.getEmail() != null && !req.getEmail().isBlank();
        if (wantsLogin) {
            trabalhadorRepository.findByEmailIgnoreCase(req.getEmail())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> { throw new IllegalStateException("Já existe um trabalhador com este email."); });
        }

        trabalhador.setName(req.getName());
        if (wantsLogin) {
            trabalhador.setEmail(req.getEmail());
            if (req.getPassword() != null && !req.getPassword().isBlank()) {
                trabalhador.setPasswordHash(passwordEncoder.encode(req.getPassword()));
            }
        } else {
            trabalhador.setEmail(null);
            trabalhador.setPasswordHash(null);
        }
        trabalhador.setResponsabilidades(toResponsabilidades(req.getResponsabilidades()));
        if (req.getStartDate() != null) trabalhador.setStartDate(req.getStartDate());
        trabalhador.setEndDate(req.getEndDate());
        return trabalhadorRepository.save(trabalhador);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!trabalhadorRepository.existsById(id)) {
            throw new IllegalArgumentException("Trabalhador não encontrado: " + id);
        }
        trabalhadorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Set<Responsabilidade> toResponsabilidades(List<ResponsabilidadeRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) {
            throw new IllegalArgumentException("Selecione pelo menos uma responsabilidade.");
        }
        Set<Responsabilidade> result = new HashSet<>();
        for (ResponsabilidadeRequest r : reqs) {
            if (r.getRole() == null) continue;
            if (r.getEndDate() != null && r.getStartDate() != null && r.getEndDate().isBefore(r.getStartDate())) {
                throw new IllegalArgumentException("A data de fim da responsabilidade " + r.getRole() + " não pode ser anterior à data de início.");
            }
            result.add(new Responsabilidade(r.getRole(), r.getStartDate(), r.getEndDate()));
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Selecione pelo menos uma responsabilidade.");
        }
        return result;
    }

    @Data
    public static class ResponsabilidadeRequest {
        private Role role;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    public static class UserRequest {
        @NotBlank private String name;
        /** Opcional — se omitido, o trabalhador fica sem acesso de login (ex.: membro de júri externo). */
        private String email;
        private String password;
        @NotEmpty(message = "Selecione pelo menos uma responsabilidade.")
        private List<ResponsabilidadeRequest> responsabilidades;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
}
