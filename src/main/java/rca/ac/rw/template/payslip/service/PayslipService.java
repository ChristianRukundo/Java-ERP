package rca.ac.rw.template.payslip.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.ac.rw.template.common.enums.PayslipStatus;
import rca.ac.rw.template.common.exceptions.ResourceNotFoundException;
import rca.ac.rw.template.common.exceptions.ValidationException; // For business rule violations
import rca.ac.rw.template.email.EmailService;
import rca.ac.rw.template.employee.entity.Employee;
import rca.ac.rw.template.payslip.converter.PayslipConverter;
import rca.ac.rw.template.payslip.dto.PayslipResponseDto;
import rca.ac.rw.template.payslip.entity.Payslip;
import rca.ac.rw.template.payslip.repository.PayslipRepository;
import rca.ac.rw.template.payslip.specs.PayslipSpecifications;
// import rca.ac.rw.template.message.service.MessageLogService; // If app were to log message instead of trigger

import java.util.List;
import java.util.UUID; // For Employee ID
import java.util.stream.Collectors;

/**
 * Service for managing and retrieving Payslip records.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayslipService {

    private final PayslipRepository payslipRepository;
    private final EmailService emailService;
    // private final MessageLogService messageLogService; // If application were to log messages

    /**
     * Retrieves a specific payslip by its ID.
     * Performs authorization check: Employee can only see their own payslip.
     * Managers/Admins can view any payslip.
     *
     * @param payslipId            The ID of the payslip to retrieve.
     * @param authenticatedEmployee The currently authenticated employee making the request.
     * @return {@link PayslipResponseDto} of the found payslip.
     * @throws ResourceNotFoundException if the payslip is not found.
     * @throws ValidationException      if an employee tries to access another employee's payslip.
     */
    @Transactional(readOnly = true)
    public PayslipResponseDto getPayslipByIdForUser(Long payslipId, Employee authenticatedEmployee) {
        log.debug("User {} (Role: {}) attempting to retrieve payslip ID: {}",
                authenticatedEmployee.getEmail(), authenticatedEmployee.getRole(), payslipId);

        Payslip payslip = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip", "ID", payslipId));

        // Authorization Check
        if (authenticatedEmployee.getRole() == rca.ac.rw.template.common.enums.Role.ROLE_EMPLOYEE &&
                !payslip.getEmployee().getId().equals(authenticatedEmployee.getId())) {
            log.warn("SECURITY ALERT: Employee {} (ID: {}) attempted to access payslip ID {} belonging to employee {}.",
                    authenticatedEmployee.getEmail(), authenticatedEmployee.getId(),
                    payslipId, payslip.getEmployee().getEmployeeCode());
            // Return a generic not found for employees to not reveal existence of other payslips
            throw new ResourceNotFoundException("Payslip", "ID (access denied)", payslipId);
        }
        // Managers and Admins can view any payslip fetched by ID.

        return PayslipConverter.toDto(payslip);
    }

    /**
     * Retrieves a paginated list of payslips for the currently authenticated employee.
     *
     * @param employee The authenticated employee.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link PayslipResponseDto}.
     */
    @Transactional(readOnly = true)
    public Page<PayslipResponseDto> getMyPayslips(Employee employee, Pageable pageable) {
        log.debug("Fetching payslip history for employee: {} (Code: {})", employee.getEmail(), employee.getEmployeeCode());
        Page<Payslip> payslipPage = payslipRepository.findByEmployeeOrderByYearDescMonthDesc(employee, pageable);

        List<PayslipResponseDto> dtos = payslipPage.getContent().stream()
                .map(PayslipConverter::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, payslipPage.getTotalElements());
    }

    /**
     * Retrieves all payslips for a given month and year, intended for Manager/Admin view.
     * Can be filtered by payslip status.
     *
     * @param year    The payroll year.
     * @param month   The payroll month.
     * @param status  Optional {@link PayslipStatus} to filter by.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link PayslipResponseDto}.
     */
    @Transactional(readOnly = true)
    public Page<PayslipResponseDto> getAllPayslipsByMonthYearForManager(int year, int month, PayslipStatus status, Pageable pageable) {
        log.debug("Manager/Admin fetching payslips for Year: {}, Month: {}, Status filter: {}", year, month, status);
        Specification<Payslip> spec = PayslipSpecifications.filterPayslips(year, month, status, null); // null for employeeId, gets all
        Page<Payslip> payslipPage = payslipRepository.findAll(spec, pageable);

        List<PayslipResponseDto> dtos = payslipPage.getContent().stream()
                .map(PayslipConverter::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, payslipPage.getTotalElements());
    }

    /**
     * Admin approves the payroll for a given month and year.
     * This changes the status of all PENDING payslips for that period to PAID.
     * After updating status, it triggers email notifications.
     * The database trigger (on `payslips` table AFTER UPDATE where status becomes PAID)
     * is responsible for inserting records into the `message_logs` table.
     *
     * @param year  The payroll year to approve.
     * @param month The payroll month to approve.
     * @return A summary string of the approval process.
     */
    @Transactional
    public String approvePayrollByAdmin(int year, int month) {
        log.info("ADMIN Action: Approving payroll for period: {}/{}", String.format("%02d", month), year);
        List<Payslip> pendingPayslips = payslipRepository.findByYearAndMonthAndStatus(year, month, PayslipStatus.PENDING);

        if (pendingPayslips.isEmpty()) {
            log.warn("No PENDING payslips found for period {}/{} to approve.", String.format("%02d", month), year);
            return "No PENDING payslips found for " + String.format("%02d/%d", month, year) + " to approve.";
        }

        int approvedCount = 0;
        for (Payslip payslip : pendingPayslips) {
            payslip.setStatus(PayslipStatus.PAID);
            Payslip savedPayslip = payslipRepository.save(payslip); // This UPDATE triggers the DB message log
            approvedCount++;

            // Send email notification to the employee
            Employee employee = savedPayslip.getEmployee();
            if (employee != null && employee.getEmail() != null) {
                // TODO: The <INSTITUTION> part of the message needs to be sourced.
                // It could be from employee.getEmploymentRecords() -> find active -> getDepartment/Institution,
                // or a global application property. For now, using a placeholder.
                String institutionName = employee.getEmploymentRecords().stream()
                        .filter(emp -> emp.getStatus() == rca.ac.rw.template.common.enums.EmploymentStatus.ACTIVE)
                        .findFirst()
                        .map(rca.ac.rw.template.employment.entity.Employment::getDepartment) // Assuming department is institution
                        .orElse("Your Institution");


                emailService.sendSalaryCreditedEmail(
                        employee.getEmail(),
                        employee.getFirstName(), // Just FirstName as per requirement "Dear <FIRSTNAME>"
                        String.format("%02d/%d", savedPayslip.getMonth(), savedPayslip.getYear()), // MONTH/YEAR
                        institutionName,
                        savedPayslip.getNetSalary(), // AMOUNT
                        employee.getEmployeeCode()   // EMPLOYEE ID (business key)
                );
            } else {
                log.warn("Could not send salary credited email for payslip ID {} as employee or email is missing.", savedPayslip.getId());
            }
        }
        log.info("Successfully approved and marked {} payslips as PAID for period {}/{}. Email notifications initiated.",
                approvedCount, String.format("%02d", month), year);
        return String.format("%d payslips for %02d/%d approved. Status set to PAID and notifications sent.",
                approvedCount, month, year);
    }
}