package pt.ipma.recrutamento.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ipma.recrutamento.domain.Vaga;
import pt.ipma.recrutamento.service.VagaService;
import pt.ipma.recrutamento.service.WorkflowEngineService;
import pt.ipma.recrutamento.web.dto.VagaCreateRequest;

/** BackOffice RH — Abertura, publicação e gestão do pipeline da Vaga (secção 3 da spec). */
@RestController
@RequestMapping("/api/backoffice/vagas")
@RequiredArgsConstructor
public class BackOfficeVagaController {

    private final VagaService vagaService;
    private final WorkflowEngineService workflowEngineService;

    @PostMapping
    public ResponseEntity<Vaga> create(@Valid @RequestBody VagaCreateRequest request) {
        return ResponseEntity.ok(vagaService.create(request));
    }

    @GetMapping("/{id}")
    public Vaga get(@PathVariable Long id) {
        return vagaService.getOrThrow(id);
    }

    /** Listagem interna de todas as vagas (qualquer estado) para o BackOffice. */
    @GetMapping
    public java.util.List<Vaga> listAll() {
        return vagaService.listAll();
    }

    @PostMapping("/{id}/publish")
    public Vaga publish(@PathVariable Long id, @RequestParam String vagaCode) {
        return vagaService.publish(id, vagaCode);
    }

    /** Botão "Concluir Triagem Provisória" (secção 6.6.2). */
    @PostMapping("/{id}/concluir-triagem")
    public ResponseEntity<String> concluirTriagem(@PathVariable Long id) {
        Vaga vaga = vagaService.getOrThrow(id);
        boolean ativouRecolha = workflowEngineService.evaluateMissingRequirementsStage(vaga);
        return ResponseEntity.ok(ativouRecolha
                ? "Fase 'Recolha de Requisitos em Falta' ativada."
                : "Sem candidatos excluídos — procedimento avançou diretamente para Avaliação.");
    }

    @PostMapping("/{id}/avancar-etapa")
    public ResponseEntity<Void> advance(@PathVariable Long id) {
        workflowEngineService.advanceStage(vagaService.getOrThrow(id));
        return ResponseEntity.ok().build();
    }
}
