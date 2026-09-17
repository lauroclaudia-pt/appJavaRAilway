package pt.ipma.recrutamento.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pt.ipma.recrutamento.domain.enums.OptionCategory;

import java.time.LocalDateTime;

@Data
public class SelectOptionRequest {
    @NotNull private OptionCategory category;
    @NotBlank private String value;
    @NotBlank private String label;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer sortOrder;
}
