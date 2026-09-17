package pt.ipma.recrutamento.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkLocationRequest {
    @NotBlank private String displayName;
    @NotBlank private String district;
    @NotBlank private String municipality;
    private String address;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
