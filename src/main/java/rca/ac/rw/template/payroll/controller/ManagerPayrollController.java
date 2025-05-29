package rca.ac.rw.template.payroll.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rca.ac.rw.template.payroll.dto.ProcessPayrollRequestDto;
import rca.ac.rw.template.payroll.service.PayrollService;

@RestController
@RequestMapping("/api/v1/manager/payroll")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_MANAGER')")
@Slf4j
public class ManagerPayrollController {

    private final PayrollService payrollService;

    /**
     * POST /api/v1/manager/payroll/process : Manager initiates payroll processing for a given month/year.
     * (Task 4 - Part 1)
     */
    @PostMapping("/process")
    public ResponseEntity<String> processPayroll(
            @Valid @RequestBody ProcessPayrollRequestDto processPayrollRequestDto) {
        log.info("Manager request to process payroll for month: {}, year: {}",
                processPayrollRequestDto.getMonth(), processPayrollRequestDto.getYear());
        String result = payrollService.processPayrollForMonth(processPayrollRequestDto);
        return ResponseEntity.ok(result);
    }
}