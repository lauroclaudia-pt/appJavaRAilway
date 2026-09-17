package pt.ipma.recrutamento.web.dto;

import lombok.Data;

@Data
public class AttachmentUpdateRequest {
    private String documentType;
    private String description;
    private boolean publicDocument;
}
