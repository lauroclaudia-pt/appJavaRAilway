package pt.ipma.recrutamento.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.ipma.recrutamento.domain.Applicant;
import pt.ipma.recrutamento.service.AppealService;
import pt.ipma.recrutamento.service.ApplicantService;
import pt.ipma.recrutamento.web.dto.AppealSubmitRequest;
import pt.ipma.recrutamento.web.dto.ApplicationSubmitRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Portal do Candidato (secções 5, 7 e 16.1.3/16.1.4 da spec).
 * Endpoints de submissão são públicos (o próprio formulário de candidatura não exige
 * conta prévia); o acompanhamento pós-submissão usa autenticação de PORTAL.
 */
@RestController
@RequiredArgsConstructor
public class PortalController {

    private final ApplicantService applicantService;
    private final AppealService appealService;

    /** Submissão do formulário de candidatura (público — secção 5.8). */
    @PostMapping(value = "/api/public/jobs/{jobId}/apply", consumes = "multipart/form-data")
    public ResponseEntity<Applicant> apply(
            @PathVariable Long jobId,
            @Valid @ModelAttribute ApplicationSubmitRequest request,
            @RequestParam(required = false) MultipartFile cv,
            @RequestParam(required = false) MultipartFile attachment_habilit,
            @RequestParam(required = false) MultipartFile attachment_decservico,
            @RequestParam(required = false) MultipartFile attachment_disability,
            @RequestParam(required = false) MultipartFile attachment_training) throws IOException {

        Map<String, MultipartFile> files = new HashMap<>();
        putIfPresent(files, "cv", cv);
        putIfPresent(files, "attachment_habilit", attachment_habilit);
        putIfPresent(files, "attachment_decservico", attachment_decservico);
        putIfPresent(files, "attachment_disability", attachment_disability);
        putIfPresent(files, "attachment_training", attachment_training);

        Applicant applicant = applicantService.submit(jobId, request, files);
        return ResponseEntity.ok(applicant);
    }

    /** Acompanhamento do estado da candidatura (secção 16.1.3). */
    @GetMapping("/api/portal/applicants/{id}")
    public Applicant myApplication(@PathVariable Long id) {
        return applicantService.getOrThrow(id);
    }

    /** Submissão de alegação / resposta à Recolha de Requisitos em Falta (secção 7.3). */
    @PostMapping("/api/portal/applicants/{id}/appeal")
    public ResponseEntity<?> submitAppeal(@PathVariable Long id, @Valid @RequestBody AppealSubmitRequest request) {
        return ResponseEntity.ok(appealService.submitFromPortal(id, request));
    }

    private void putIfPresent(Map<String, MultipartFile> map, String key, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            map.put(key, file);
        }
    }
}
