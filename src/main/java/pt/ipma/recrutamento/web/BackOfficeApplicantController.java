package pt.ipma.recrutamento.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.ipma.recrutamento.domain.Applicant;
import pt.ipma.recrutamento.domain.Attachment;
import pt.ipma.recrutamento.domain.Vaga;
import pt.ipma.recrutamento.repository.AttachmentRepository;
import pt.ipma.recrutamento.service.ApplicantService;
import pt.ipma.recrutamento.service.AtaPdfService;
import pt.ipma.recrutamento.service.VagaService;
import pt.ipma.recrutamento.web.dto.TriagemRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/** BackOffice RH — Verificação de Admitidos, geração de Atas e notificações (secções 6 e 15.5). */
@RestController
@RequestMapping("/api/backoffice")
@RequiredArgsConstructor
public class BackOfficeApplicantController {

    private final ApplicantService applicantService;
    private final VagaService vagaService;
    private final AtaPdfService ataPdfService;
    private final AttachmentRepository attachmentRepository;

    @GetMapping("/vagas/{jobId}/candidatos")
    public List<Applicant> listApplicants(@PathVariable Long jobId) {
        return applicantService.listByJob(jobId);
    }

    @PostMapping("/candidatos/{id}/triagem")
    public Applicant triagem(@PathVariable Long id, @RequestBody TriagemRequest request) {
        return applicantService.triagem(id, request);
    }

    /** Botão "Notificar e Publicar" (secção 6.6.1). */
    @PostMapping("/vagas/{jobId}/notificar-publicar")
    public ResponseEntity<Void> notificarPublicar(@PathVariable Long jobId,
                                                   @RequestParam(defaultValue = "10") int prazoDias) {
        applicantService.notifyAndPublish(jobId, prazoDias);
        return ResponseEntity.ok().build();
    }

    /** Botão "Criar Ata" — gera e devolve o PDF da Lista Provisória/Final (secção 6.6.1). */
    @GetMapping(value = "/vagas/{jobId}/ata", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> gerarAta(@PathVariable Long jobId,
                                            @RequestParam(defaultValue = "Ata Provisória — Lista de Admitidos e Excluídos") String titulo,
                                            @RequestParam(defaultValue = "false") boolean guardarComoAnexo) throws IOException {
        Vaga vaga = vagaService.getOrThrow(jobId);
        List<Applicant> applicants = applicantService.listByJob(jobId);
        byte[] pdf = ataPdfService.gerarAtaListaAdmitidosExcluidos(vaga, applicants, titulo);

        if (guardarComoAnexo) {
            Attachment attachment = new Attachment();
            attachment.setFilename("Ata_" + vaga.getVagaCode() + "_" + LocalDateTime.now() + ".pdf");
            attachment.setContentType(MediaType.APPLICATION_PDF_VALUE);
            attachment.setData(pdf);
            attachment.setResModel("vaga");
            attachment.setResId(jobId);
            attachment.setDocumentType("ata_provisoria");
            attachment.setPublicDocument(false); // só passa a público após upload da versão assinada
            attachmentRepository.save(attachment);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=ata.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
