package pt.ipma.recrutamento.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pt.ipma.recrutamento.domain.Trabalhador;
import pt.ipma.recrutamento.domain.enums.Role;
import pt.ipma.recrutamento.repository.TrabalhadorRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Administração de trabalhadores: um trabalhador pode ter zero, uma ou várias
 * responsabilidades (ADMIN, CDRH, GESTOR_RH, JURI); o login (email/password) é
 * opcional; o estado (Ativo/Inativo) é calculado a partir de startDate/endDate.
 * Acesso restrito a ADMIN.
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
    public Trabalhador create(@org.springframework.web.bind.annotation.RequestBody UserRequest req) {
        boolean wantsLogin = req.getEmail() != null && !req.getEmail().isBlank();

        if (wantsLogin && trabalhadorRepository.findByEmailIgnoreCase(req.getEmail()).isPresent()) {
            throw new IllegalStateException("Já existe um trabalhador com este email.");
        }
        if (wantsLogin && (req.getPassword() == null || req.getPassword().isBlank())) {
            throw new IllegalArgumentException("É necessário definir uma palavra-passe quando é indicado um email de acesso.");
        }
        validateDates(req);

        Trabalhador trabalhador = new Trabalhador();
        trabalhador.setName(req.getName());
        if (wantsLogin) {
            trabalhador.setEmail(req.getEmail());
            trabalhador.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        trabalhador.setResponsabilidades(req.getRoles());
        trabalhador.setStartDate(req.getStartDate() != null ? req.getStartDate() : LocalDateTime.now());
        trabalhador.setEndDate(req.getEndDate());
        return trabalhadorRepository.save(trabalhador);
    }

    @PutMapping("/{id}")
    public Trabalhador update(@PathVariable Long id, @org.springframework.web.bind.annotation.RequestBody UserRequest req) {
        Trabalhador trabalhador = trabalhadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trabalhador não encontrado: " + id));

        boolean wantsLogin = req.getEmail() != null && !req.getEmail().isBlank();
        if (wantsLogin) {
            trabalhadorRepository.findByEmailIgnoreCase(req.getEmail())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> { throw new IllegalStateException("Já existe um trabalhador com este email."); });
        }
        validateDates(req);

        trabalhador.setName(req.getName());
        if (wantsLogin) {
            trabalhador.setEmail(req.getEmail());
            // A palavra-passe só é alterada se for indicada; em branco mantém a atual.
            if (req.getPassword() != null && !req.getPassword().isBlank()) {
                trabalhador.setPasswordHash(passwordEncoder.encode(req.getPassword()));
            }
        } else {
            trabalhador.setEmail(null);
            trabalhador.setPasswordHash(null);
        }
        trabalhador.setResponsabilidades(req.getRoles());
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

    private void validateDates(UserRequest req) {
        if (req.getStartDate() != null && req.getEndDate() != null && !req.getEndDate().isAfter(req.getStartDate())) {
            throw new IllegalArgumentException("A data de fim tem de ser posterior à data de início.");
        }
    }

    @Data
    public static class UserRequest {
        @NotBlank private String name;
        /** Opcional — se omitido, o trabalhador fica sem acesso de login (ex.: membro de júri externo). */
        private String email;
        private String password;
        @NotEmpty(message = "Selecione pelo menos uma responsabilidade.")
        private Set<Role> roles;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
}
