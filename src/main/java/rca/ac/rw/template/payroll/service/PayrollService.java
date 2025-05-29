package rca.ac.rw.template.payroll.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.ac.rw.template.common.enums.EmployeeStatus;
import rca.ac.rw.template.common.enums.EmploymentStatus;
import rca.ac.rw.template.common.enums.PayslipStatus;
import rca.ac.rw.template.common.exceptions.BadRequestException;
import rca.ac.rw.template.common.exceptions.ValidationException;
import rca.ac.rw.template.deduction.entity.Deduction;
import rca.ac.rw.template.deduction.service.DeductionService;
import rca.ac.rw.template.employee.entity.Employee;
import rca.ac.rw.template.employee.repository.EmployeeRepository;
import rca.ac.rw.template.employment.entity.Employment;
import rca.ac.rw.template.employment.repository.EmploymentRepository;
import rca.ac.rw.template.payslip.entity.Payslip;
import rca.ac.rw.template.payslip.repository.PayslipRepository;
import rca.ac.rw.template.payroll.dto.ProcessPayrollRequestDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service responsible for processing payroll, calculating salaries,
 * and generating payslip records.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollService {

    private final EmployeeRepository employeeRepository;
    private final EmploymentRepository employmentRepository;
    private final DeductionService deductionService;
    private final PayslipRepository payslipRepository;

    // Standardized deduction codes (ensure these match what's in your Deduction table)
    public static final String HOUSING_CODE = "HOUSING_ALLOWANCE"; // Example standard codes
    public static final String TRANSPORT_CODE = "TRANSPORT_ALLOWANCE";
    public static final String EMPLOYEE_TAX_CODE = "EMPLOYEE_TAX";
    public static final String PENSION_CODE = "PENSION"; // New rate 6%
    public static final String MEDICAL_CODE = "MEDICAL_INSURANCE";
    public static final String OTHERS_CODE = "OTHERS";

    private static final int SALARY_CALCULATION_SCALE = 2; // For RWF, 2 decimal places for final amounts
    private static final RoundingMode SALARY_ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Processes payroll for all active employees with active employment for a given month and year.
     * Generates PENDING payslips.
     *
     * @param requestDto Containing the year and month for payroll.
     * @return A summary string indicating the outcome of the payroll process.
     * @throws ValidationException if required deduction configurations are missing.
     */
    @Transactional
    public String processPayrollForMonth(ProcessPayrollRequestDto requestDto) {
        int year = requestDto.getYear();
        int month = requestDto.getMonth();
        log.info("Manager initiating payroll processing for Year: {}, Month: {}", year, month);

        // 1. Fetch all active deduction percentages and map them by their code
        List<Deduction> activeDeductionsList = deductionService.getAllActiveDeductionsForPayroll();
        Map<String, BigDecimal> deductionsMap = activeDeductionsList.stream()
                .collect(Collectors.toMap(Deduction::getCode, Deduction::getPercentage));
        validateRequiredDeductions(deductionsMap);

        // 2. Fetch all active employees with currently active employment records
        // The repository method needs to handle this join and filtering.
        List<Employee> employeesToProcess = employeeRepository.findActiveEmployeesWithActiveEmployment(
                EmployeeStatus.ACTIVE, EmploymentStatus.ACTIVE
        );

        if (employeesToProcess.isEmpty()) {
            log.warn("No active employees with active employment records found for payroll processing {}-{}.", month,year);
            return "No active employees found for payroll processing for " + String.format("%02d/%d", month, year) + ".";
        }

        int payslipsCreatedCount = 0;
        int employeesSkippedCount = 0;
        int employeesWithErrorsCount = 0;

        for (Employee employee : employeesToProcess) {
            try {
                // 3. Prevent duplicate payroll generation for the same employee in the same month/year
                if (payslipRepository.existsByEmployeeAndYearAndMonth(employee, year, month)) {
                    log.warn("Payroll for employee {} (Code: {}) for {}/{} already exists. Skipping.",
                            employee.getEmail(), employee.getEmployeeCode(), month, year);
                    employeesSkippedCount++;
                    continue;
                }

                // 4. Get current active employment record for base salary
                // The list from findActiveEmployeesWithActiveEmployment should ideally give us employees
                // who definitely have one active employment record. If an employee could have multiple
                // 'ACTIVE' employments (which should be a data error), we take the latest one.
                Employment employment = employmentRepository.findCurrentActiveEmployment(employee)
                        .orElseThrow(() -> new ValidationException("No active employment record found for employee " +
                                employee.getEmployeeCode() + " despite being selected for payroll. Data inconsistency."));

                BigDecimal baseSalary = employment.getBaseSalary();
                if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) <= 0) {
                    log.warn("Employee {} (Code: {}) has a zero or null base salary in their active employment record. Skipping payroll.",
                            employee.getEmail(), employee.getEmployeeCode());
                    employeesSkippedCount++;
                    continue;
                }

                // 5. Calculate Allowances (as per document example these are part of gross, not separate deductions from gross)
                BigDecimal housingAllowance = calculatePercentage(baseSalary, deductionsMap.get(HOUSING_CODE));
                BigDecimal transportAllowance = calculatePercentage(baseSalary, deductionsMap.get(TRANSPORT_CODE));

                // 6. Calculate Gross Salary
                BigDecimal grossSalary = baseSalary.add(housingAllowance).add(transportAllowance);

                // 7. Calculate Deductions (based on baseSalary as specified)
                BigDecimal employeeTaxAmount = calculatePercentage(baseSalary, deductionsMap.get(EMPLOYEE_TAX_CODE));
                BigDecimal pensionAmount = calculatePercentage(baseSalary, deductionsMap.get(PENSION_CODE)); // New 6% rate
                BigDecimal medicalAmount = calculatePercentage(baseSalary, deductionsMap.get(MEDICAL_CODE));
                BigDecimal othersAmount = calculatePercentage(baseSalary, deductionsMap.get(OTHERS_CODE));

                BigDecimal totalDeductionsFromBase = employeeTaxAmount.add(pensionAmount).add(medicalAmount).add(othersAmount);

                // 8. Calculate Net Salary
                BigDecimal netSalary = grossSalary.subtract(totalDeductionsFromBase);


                if (netSalary.compareTo(BigDecimal.ZERO) < 0) {
                    log.error("Calculated net salary for employee {} (Code: {}) is negative ({}). Base: {}, Gross: {}, Deductions: {}. Skipping payslip.",
                            employee.getEmail(), employee.getEmployeeCode(), netSalary, baseSalary, grossSalary, totalDeductionsFromBase);
                    employeesWithErrorsCount++;
                    continue; // Skip this employee, log an error for investigation
                }

                // 10. Create and Save Payslip
                Payslip payslip = new Payslip();
                payslip.setEmployee(employee);
                payslip.setYear(year);
                payslip.setMonth(month);
                payslip.setBaseSalarySnapshot(baseSalary);
                payslip.setHouseAmount(housingAllowance);
                payslip.setTransportAmount(transportAllowance);
                payslip.setGrossSalary(grossSalary);
                payslip.setEmployeeTaxedAmount(employeeTaxAmount);
                payslip.setPensionAmount(pensionAmount);
                payslip.setMedicalInsuranceAmount(medicalAmount);
                payslip.setOtherTaxedAmount(othersAmount);
                payslip.setNetSalary(netSalary);
                payslip.setStatus(PayslipStatus.PENDING); // Initial status

                payslipRepository.save(payslip);
                payslipsCreatedCount++;

            } catch (Exception e) {
                log.error("Error processing payroll for employee {} (Code: {}): {}",
                        employee.getEmail(), employee.getEmployeeCode(), e.getMessage(), e);
                employeesWithErrorsCount++;
            }
        }

        log.info("Payroll processing complete for {}/{}. Payslips created: {}, Employees skipped: {}, Errors: {}",
                month, year, payslipsCreatedCount, employeesSkippedCount, employeesWithErrorsCount);
        return String.format("Payroll processed for %s/%s. Created: %d, Skipped: %d, Errors: %d.",
                String.format("%02d", month), year, payslipsCreatedCount, employeesSkippedCount, employeesWithErrorsCount);
    }

    private BigDecimal calculatePercentage(BigDecimal base, BigDecimal percentage) {
        if (base == null || percentage == null) {
            // Or throw an error if a required deduction percentage is missing (handled by validateRequiredDeductions)
            return BigDecimal.ZERO;
        }
        return base.multiply(percentage).setScale(SALARY_CALCULATION_SCALE, SALARY_ROUNDING_MODE);
    }

    private void validateRequiredDeductions(Map<String, BigDecimal> deductionsMap) {
        String[] requiredCodes = {
                HOUSING_CODE, TRANSPORT_CODE, EMPLOYEE_TAX_CODE,
                PENSION_CODE, MEDICAL_CODE, OTHERS_CODE
        };
        for (String code : requiredCodes) {
            if (!deductionsMap.containsKey(code) || deductionsMap.get(code) == null) {
                log.error("Critical payroll configuration error: Required deduction with code '{}' is missing or has a null percentage.", code);
                throw new ValidationException("Required deduction configuration missing for code: " + code +
                        ". Please ensure all standard deductions are defined, active, and have a percentage.");
            }
        }
    }
}