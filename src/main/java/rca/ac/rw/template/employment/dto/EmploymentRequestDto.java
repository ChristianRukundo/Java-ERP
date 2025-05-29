package rca.ac.rw.template.employment.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.common.enums.EmploymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentRequestDto {

    @NotNull(message = "Employee ID is required") // Employee's technical UUID id
    private UUID employeeId;

    @NotBlank(message = "Department is required")
    @Size(max = 100)
    private String department;

    @NotBlank(message = "Position is required")
    @Size(max = 100)
    private String position;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base salary must be positive")
    private BigDecimal baseSalary;

    @NotNull(message = "Employment status is required")
    private EmploymentStatus status = EmploymentStatus.ACTIVE;

    @NotNull(message = "Joining date is required")
    @PastOrPresent(message = "Joining date cannot be in the future")
    private LocalDate joiningDate;

    @PastOrPresent(message = "End date cannot be in the future and must be after joining date if present")
    private LocalDate endDate; // Optional
}