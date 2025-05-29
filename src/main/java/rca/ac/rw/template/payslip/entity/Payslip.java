package rca.ac.rw.template.payslip.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import rca.ac.rw.template.audits.TimestampAudit; // For createdAt (generationDate) and updatedAt
import rca.ac.rw.template.common.enums.PayslipStatus; // New Enum
import rca.ac.rw.template.employee.entity.Employee;

import java.math.BigDecimal;

@Entity
@Table(name = "payslips", indexes = {
        @Index(name = "idx_payslip_employee_month_year", columnList = "employee_id, pay_year, pay_month", unique = true)
        // unique = true ensures no duplicate payroll for same employee, month, year
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Payslip extends TimestampAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull(message = "Payroll year is required")
    @Min(2000) @Max(2999)
    @Column(name = "pay_year", nullable = false)
    private Integer year;

    @NotNull(message = "Payroll month is required")
    @Min(1) @Max(12)
    @Column(name = "pay_month", nullable = false)
    private Integer month;

    @NotNull
    @Column(precision = 19, scale = 2)
    private BigDecimal baseSalarySnapshot; // Base salary at the time of payroll run

    @NotNull
    @Column(precision = 19, scale = 2)
    private BigDecimal grossSalary;

    @NotNull
    @Column(precision = 19, scale = 2)
    private BigDecimal netSalary;

    @NotNull
    @Column(precision = 19, scale = 2)
    private BigDecimal houseAmount;

    @NotNull
    @Column(precision = 19, scale = 2)
    private BigDecimal transportAmount;

    @NotNull
    @Column(name = "employee_tax_amount", precision = 19, scale = 2)
    private BigDecimal employeeTaxedAmount;

    @NotNull
    @Column(name = "pension_amount", precision = 19, scale = 2)
    private BigDecimal pensionAmount;

    @NotNull
    @Column(name = "medical_insurance_amount", precision = 19, scale = 2)
    private BigDecimal medicalInsuranceAmount;

    @NotNull
    @Column(name = "other_taxed_amount", precision = 19, scale = 2)
    private BigDecimal otherTaxedAmount; // 'Others' deduction

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayslipStatus status = PayslipStatus.PENDING;
}