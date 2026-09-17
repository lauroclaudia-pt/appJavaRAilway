package pt.ipma.recrutamento.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pt.ipma.recrutamento.domain.AppUser;
import pt.ipma.recrutamento.domain.enums.Role;
import pt.ipma.recrutamento.repository.AppUserRepository;

import java.util.List;

/** Administração de utilizadores do sistema (Gestor RH, Júri, CDRH) — acesso restrito a ADMIN. */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public List<AppUser> list() {
        return appUserRepository.findAll();
    }

    @PostMapping
    public AppUser create(@org.springframework.web.bind.annotation.RequestBody CreateUserRequest req) {
        if (appUserRepository.findByEmailIgnoreCase(req.getEmail()).isPresent()) {
            throw new IllegalStateException("Já existe um utilizador com este email.");
        }
        AppUser user = new AppUser();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        return appUserRepository.save(user);
    }

    @Data
    public static class CreateUserRequest {
        @NotBlank private String name;
        @NotBlank private String email;
        @NotBlank private String password;
        @NotNull private Role role;
    }
}
