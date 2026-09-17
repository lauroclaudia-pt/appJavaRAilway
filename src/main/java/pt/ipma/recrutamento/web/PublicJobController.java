package pt.ipma.recrutamento.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ipma.recrutamento.domain.Attachment;
import pt.ipma.recrutamento.domain.Vaga;
import pt.ipma.recrutamento.repository.AttachmentRepository;
import pt.ipma.recrutamento.service.VagaService;

import java.util.List;

/** Website público — Listagem e Detalhe de Ofertas (secção 4 / 16.1 da spec). Sem autenticação. */
@RestController
@RequestMapping("/api/public/jobs")
@RequiredArgsConstructor
public class PublicJobController {

    private final VagaService vagaService;
    private final AttachmentRepository attachmentRepository;

    @GetMapping
    public List<Vaga> listPublished() {
        return vagaService.listPublished();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vaga> detail(@PathVariable Long id) {
        Vaga vaga = vagaService.getOrThrow(id);
        if (!vaga.isVisibleOnWebsite()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vaga);
    }

    /** Documentos publicados na oferta (Aviso DRE, Detalhe BEP, Atas) — só os marcados como públicos. */
    @GetMapping("/{id}/documents")
    public List<Attachment> publicDocuments(@PathVariable Long id) {
        return attachmentRepository.findByResModelAndResIdAndPublicDocumentTrue("vaga", id);
    }
}
