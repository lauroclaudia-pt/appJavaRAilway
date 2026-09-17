package pt.ipma.recrutamento.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ipma.recrutamento.domain.SelectOption;
import pt.ipma.recrutamento.domain.enums.OptionCategory;
import pt.ipma.recrutamento.service.SelectOptionService;
import pt.ipma.recrutamento.web.dto.SelectOptionRequest;

import java.util.List;

/**
 * Administração das opções configuráveis dos campos select da Vaga/Candidatura
 * (Vínculo, Regime, Nível Habilitacional, Situação Profissional, Relação de
 * Vagas/Candidaturas). Acesso restrito a ADMIN — ver SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/options")
@RequiredArgsConstructor
public class AdminSelectOptionController {

    private final SelectOptionService service;

    @GetMapping
    public List<SelectOption> list(@RequestParam OptionCategory category) {
        return service.listAll(category);
    }

    @PostMapping
    public ResponseEntity<SelectOption> create(@Valid @RequestBody SelectOptionRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PutMapping("/{id}")
    public SelectOption update(@PathVariable Long id, @Valid @RequestBody SelectOptionRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
