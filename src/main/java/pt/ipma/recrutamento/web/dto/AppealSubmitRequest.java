package pt.ipma.recrutamento.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppealSubmitRequest {
    @NotBlank private String phase; // missing_requirements | appeal
    @NotBlank private String allegations;
}
