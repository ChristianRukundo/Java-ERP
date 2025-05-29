package rca.ac.rw.template.deduction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeductionResponseDto {
    private UUID id;
    private String code;
    private String deductionName;
    private BigDecimal percentage; // e.g., 0.30
    private boolean isActive;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}