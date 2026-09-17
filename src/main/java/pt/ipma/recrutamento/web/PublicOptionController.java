package pt.ipma.recrutamento.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.ipma.recrutamento.domain.SelectOption;
import pt.ipma.recrutamento.domain.enums.OptionCategory;
import pt.ipma.recrutamento.service.SelectOptionService;

import java.util.List;
import java.util.Map;

/**
 * Leitura pública das opções atualmente válidas, para popular campos select nos
 * formulários (ex.: Vínculo, Regime, Nível Habilitacional). Sem autenticação —
 * expõe apenas rótulo/código, nunca dados sensíveis.
 */
@RestController
@RequestMapping("/api/public/options")
@RequiredArgsConstructor
public class PublicOptionController {

    private final SelectOptionService service;

    @GetMapping
    public List<Map<String, String>> listActive(@RequestParam OptionCategory category) {
        return service.listActive(category).stream()
                .map(o -> Map.of("value", o.getValue(), "label", o.getLabel()))
                .toList();
    }
}
