package rca.ac.rw.template.deduction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.audits.InitiatorAudit; // Or TimestampAudit if createdBy/updatedBy not needed

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "deductions", indexes = {
        @Index(name = "idx_deduction_name_unique", columnList = "deductionName", unique = true),
        @Index(name = "idx_deduction_code_unique", columnList = "code", unique = true) // If code is also a business key
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Deduction extends InitiatorAudit { // Using InitiatorAudit for full audit trail

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id; // Technical UUID primary key

    @NotBlank(message = "Deduction code is required")
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_\\-]+$", message = "Deduction code can only contain alphanumeric, underscore, and hyphen")
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank(message = "Deduction name is required")
    @Size(min = 3, max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String deductionName; // E.g., "Employee Tax", "Pension Contribution (New Rate)"

    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Percentage cannot be negative")
    @DecimalMax(value = "1.0", inclusive = true, message = "Percentage must be between 0.0 (0%) and 1.0 (100%)")
    @Column(nullable = false, precision = 5, scale = 4) // e.g., 0.3000 for 30%
    private BigDecimal percentage;

    @Column(nullable = false)
    private boolean isActive = true; // To enable/disable a deduction without deleting it

    // Optional: Description for more details about the deduction
    @Size(max = 255)
    private String description;
}