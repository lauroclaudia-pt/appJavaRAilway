package pt.ipma.recrutamento.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.ipma.recrutamento.domain.AppUser;
import pt.ipma.recrutamento.repository.AppUserRepository;

import java.util.Map;

/** Usado pelo frontend estático para confirmar a sessão HTTP Basic e obter o perfil do utilizador. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository appUserRepository;

    @GetMapping("/me")
    public Map<String, String> me(Authentication authentication) {
        AppUser user = appUserRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Utilizador autenticado não encontrado."));
        return Map.of(
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        );
    }
}
