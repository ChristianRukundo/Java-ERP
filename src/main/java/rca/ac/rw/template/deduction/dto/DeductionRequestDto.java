package rca.ac.rw.template.deduction.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeductionRequestDto {

    @NotBlank(message = "Deduction code is required")
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_\\-]+$", message = "Deduction code can only contain alphanumeric, underscore, and hyphen")
    private String code;

    @NotBlank(message = "Deduction name is required")
    @Size(min = 3, max = 100)
    private String deductionName;

    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Percentage cannot be negative (0 for 0%)")
    @DecimalMax(value = "1.0", inclusive = true, message = "Percentage must be between 0.0 (0%) and 1.0 (100%)")
    // Example: client sends 0.30 for 30%
    private BigDecimal percentage;

    private Boolean isActive; // Optional for update, defaults to true on create

    @Size(max = 255)
    private String description;
}