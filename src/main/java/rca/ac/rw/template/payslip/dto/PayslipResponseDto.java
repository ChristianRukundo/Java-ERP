package rca.ac.rw.template.payslip.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.common.enums.PayslipStatus;
import rca.ac.rw.template.employee.dto.EmployeeMiniDto; // Simplified Employee DTO

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayslipResponseDto {
    private Long id;
    private EmployeeMiniDto employee; // Basic employee info
    private Integer year;
    private Integer month;
    private BigDecimal baseSalarySnapshot;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
    private BigDecimal houseAmount;
    private BigDecimal transportAmount;
    private BigDecimal employeeTaxedAmount;
    private BigDecimal pensionAmount;
    private BigDecimal medicalInsuranceAmount;
    private BigDecimal otherTaxedAmount;
    private PayslipStatus status;
    private LocalDateTime generatedAt; // createdAt from Payslip entity
    private LocalDateTime lastUpdatedAt; // updatedAt from Payslip entity
}