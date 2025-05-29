package rca.ac.rw.template.employment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.audits.InitiatorAudit;
import rca.ac.rw.template.common.enums.EmploymentStatus;
import rca.ac.rw.template.employee.entity.Employee; // Import your Employee entity

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employments", indexes = {
        @Index(name = "idx_employment_employee_status", columnList = "employee_id, status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Employment extends InitiatorAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // UUID for employment record ID
    private UUID id; // Changed from 'code' to 'id' for JPA standard PK

    @NotNull(message = "Employee is required for an employment record")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id", nullable = false)
    private Employee employee;

    @NotBlank(message = "Department is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String department;

    @NotBlank(message = "Position is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String position;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base salary must be positive")
    @Column(name = "base_salary", precision = 19, scale = 2, nullable = false)
    private BigDecimal baseSalary;

    @NotNull(message = "Employment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentStatus status = EmploymentStatus.ACTIVE; // Default to active

    @NotNull(message = "Joining date is required")
    @PastOrPresent(message = "Joining date cannot be in the future")
    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "end_date") // Optional: if employment has ended
    private LocalDate endDate;

    // Soft delete flag (optional but recommended for historical records)
    // @Column(name = "deleted", nullable = false)
    // private boolean deleted = false;
    // If using soft delete, add @SQLDelete and @Where as in Employee entity
}