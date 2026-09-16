package pt.ipma.recrutamento.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ir.attachment equivalente — repositório central de documentos (vaga, candidato, alegação).
 * O campo isPublicDocument controla a exposição no website público (secção 2.1.3 / 4.2).
 */
@Entity
@Table(name = "attachment")
@Getter
@Setter
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String filename;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Lob
    @Column(nullable = false)
    private byte[] data;

    /** Modelo a que o anexo pertence: "vaga", "applicant", "appeal". */
    @Column(name = "res_model", nullable = false, length = 60)
    private String resModel;

    @Column(name = "res_id", nullable = false)
    private Long resId;

    /** Ex.: ata_provisoria, ata_final, aviso_dre, cv, habilitacoes, declaracao_incapacidade, outro. */
    @Column(name = "document_type", length = 60)
    private String documentType;

    @Column(name = "is_public_document", nullable = false)
    private boolean publicDocument = false;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate = LocalDateTime.now();
}
