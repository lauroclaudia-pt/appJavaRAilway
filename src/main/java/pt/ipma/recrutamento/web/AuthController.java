package pt.ipma.recrutamento.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.ipma.recrutamento.domain.Trabalhador;
import pt.ipma.recrutamento.repository.TrabalhadorRepository;

import java.util.Map;

/** Usado pelo frontend estático para confirmar a sessão HTTP Basic e obter o(s) perfil(is) do trabalhador. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TrabalhadorRepository trabalhadorRepository;

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        Trabalhador t = trabalhadorRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Trabalhador autenticado não encontrado."));
        return Map.of(
                "id", t.getId(),
                "name", t.getName(),
                "email", t.getEmail(),
                "roles", t.getResponsabilidades().stream().map(Enum::name).sorted().toList()
        );
    }
}
