package rca.ac.rw.template.payslip.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rca.ac.rw.template.auth.AuthService; // For getting authenticated Employee
import rca.ac.rw.template.employee.entity.Employee;
import rca.ac.rw.template.file.service.PdfReceiptService;
import rca.ac.rw.template.payslip.dto.PayslipResponseDto;
import rca.ac.rw.template.payslip.entity.Payslip;
import rca.ac.rw.template.payslip.repository.PayslipRepository; // To fetch for PDF
import rca.ac.rw.template.payslip.service.PayslipService;
import rca.ac.rw.template.common.exceptions.ResourceNotFoundException;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/employee/payslips")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_EMPLOYEE')") // Only employees access their own payslips
@Slf4j
public class EmployeePayslipController {

    private final PayslipService payslipService;
    private final AuthService authService; // To get current employee via JWT context
    private final PdfReceiptService pdfPayslipService;
    private final PayslipRepository payslipRepository; // To fetch full payslip for PDF generation

    /**
     * GET /api/v1/employee/payslips/my : Employee retrieves their own payslip history.
     */
    @GetMapping("/my")
    public ResponseEntity<Page<PayslipResponseDto>> getMyPayslips(
            @PageableDefault(size = 12, sort = "year", direction = Sort.Direction.DESC) Pageable pageable) {
        Employee employee = authService.getAuthenticatedEmployee(); // Gets current Employee
        log.info("Employee {} (Code: {}) fetching their payslip history.", employee.getEmail(), employee.getEmployeeCode());
        Page<PayslipResponseDto> payslips = payslipService.getMyPayslips(employee, pageable);
        return ResponseEntity.ok(payslips);
    }

    /**
     * GET /api/v1/employee/payslips/{payslipId} : Employee retrieves a specific payslip by its ID.
     */
    @GetMapping("/{payslipId}")
    public ResponseEntity<PayslipResponseDto> getMyPayslipById(@PathVariable Long payslipId) {
        Employee employee = authService.getAuthenticatedEmployee();
        log.info("Employee {} (Code: {}) fetching payslip ID: {}", employee.getEmail(), employee.getEmployeeCode(), payslipId);
        PayslipResponseDto payslip = payslipService.getPayslipByIdForUser(payslipId, employee); // Service method performs auth check
        return ResponseEntity.ok(payslip);
    }

    /**
     * GET /api/v1/employee/payslips/{payslipId}/download : Employee downloads their payslip as PDF.
     */
    @GetMapping("/{payslipId}/download")
    public ResponseEntity<byte[]> downloadMyPayslip(@PathVariable Long payslipId) throws IOException {
        Employee employee = authService.getAuthenticatedEmployee();
        log.info("Employee {} (Code: {}) requesting download for payslip ID: {}",
                employee.getEmail(), employee.getEmployeeCode(), payslipId);


        Payslip payslip = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip", "ID", payslipId));

        // Authorization check: Ensure employee can only download their own payslip
        if (!payslip.getEmployee().getId().equals(employee.getId())) {
            log.warn("SECURITY: Employee {} attempted to download payslip ID {} belonging to another employee (ID: {}).",
                    employee.getEmail(), payslipId, payslip.getEmployee().getId());
            throw new ResourceNotFoundException("Payslip", "ID " + payslipId, "(access denied)");
        }

        byte[] pdfBytes = pdfPayslipService.generatePayslipPdf(payslip); // Use dedicated method

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = String.format("Payslip_%s_%d-%02d.pdf",
                employee.getEmployeeCode(), payslip.getYear(), payslip.getMonth());
        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}