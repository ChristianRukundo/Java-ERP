package rca.ac.rw.template.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rca.ac.rw.template.payslip.service.PayslipService; // Service to approve

@RestController
@RequestMapping("/api/v1/admin/payroll")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Validated // For validating request parameters
@Slf4j
public class AdminPayrollController {

    private final PayslipService payslipService; // Use PayslipService to update status

    /**
     * POST /api/v1/admin/payroll/approve : Admin approves payroll for a given month/year.
     * This changes status of PENDING payslips to PAID and triggers notifications.
     * (Task 5a)
     */
    @PostMapping("/approve")
    public ResponseEntity<String> approvePayroll(
            @RequestParam @NotNull @Min(2020) @Max(2999) Integer year,
            @RequestParam @NotNull @Min(1) @Max(12) Integer month) {
        log.info("Admin request to approve payroll for Year: {}, Month: {}", year, month);
        String result = payslipService.approvePayrollByAdmin(year, month);
        return ResponseEntity.ok(result);
    }
}