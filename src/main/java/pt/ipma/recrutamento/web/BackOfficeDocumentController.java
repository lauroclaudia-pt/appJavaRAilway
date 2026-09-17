package pt.ipma.recrutamento.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.ipma.recrutamento.domain.Attachment;
import pt.ipma.recrutamento.repository.AttachmentRepository;
import pt.ipma.recrutamento.service.VagaService;
import pt.ipma.recrutamento.web.dto.AttachmentUpdateRequest;

import java.io.IOException;
import java.util.List;

/**
 * Consulta, edição e remoção dos documentos de um procedimento (Ata de Abertura,
 * Detalhe de Oferta BEP, Aviso DRE, etc. — secções 19 e 20). Cada documento tem
 * tipo, descrição e indicação de visibilidade pública.
 */
@RestController
@RequiredArgsConstructor
public class BackOfficeDocumentController {

    private final AttachmentRepository attachmentRepository;
    private final VagaService vagaService;

    @GetMapping("/api/backoffice/vagas/{vagaId}/documents")
    public List<Attachment> list(@PathVariable Long vagaId) {
        vagaService.getOrThrow(vagaId);
        return attachmentRepository.findByResModelAndResId("vaga", vagaId);
    }

    @PostMapping(value = "/api/backoffice/vagas/{vagaId}/documents", consumes = "multipart/form-data")
    public Attachment upload(@PathVariable Long vagaId,
                              @RequestParam MultipartFile file,
                              @RequestParam(required = false) String documentType,
                              @RequestParam(required = false) String description,
                              @RequestParam(defaultValue = "false") boolean publicDocument) throws IOException {
        var vaga = vagaService.getOrThrow(vagaId);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Selecione um ficheiro para enviar.");
        }
        Attachment attachment = new Attachment();
        attachment.setFilename(file.getOriginalFilename());
        attachment.setContentType(file.getContentType());
        attachment.setData(file.getBytes());
        attachment.setResModel("vaga");
        attachment.setResId(vaga.getId());
        attachment.setDocumentType(documentType);
        attachment.setDescription(description);
        attachment.setPublicDocument(publicDocument);
        return attachmentRepository.save(attachment);
    }

    @PutMapping("/api/backoffice/documents/{id}")
    public Attachment update(@PathVariable Long id, @RequestBody AttachmentUpdateRequest req) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + id));
        attachment.setDocumentType(req.getDocumentType());
        attachment.setDescription(req.getDescription());
        attachment.setPublicDocument(req.isPublicDocument());
        return attachmentRepository.save(attachment);
    }

    @DeleteMapping("/api/backoffice/documents/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!attachmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Documento não encontrado: " + id);
        }
        attachmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/backoffice/documents/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        Attachment a = attachmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + id));
        return downloadResponse(a);
    }

    /** Download público — apenas para documentos marcados como visíveis. */
    @GetMapping("/api/public/documents/{id}/download")
    public ResponseEntity<byte[]> downloadPublic(@PathVariable Long id) {
        Attachment a = attachmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado: " + id));
        if (!a.isPublicDocument()) {
            throw new IllegalArgumentException("Documento não encontrado: " + id);
        }
        return downloadResponse(a);
    }

    private ResponseEntity<byte[]> downloadResponse(Attachment a) {
        MediaType type = a.getContentType() != null
                ? MediaType.parseMediaType(a.getContentType())
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + a.getFilename() + "\"")
                .contentType(type)
                .body(a.getData());
    }
}
