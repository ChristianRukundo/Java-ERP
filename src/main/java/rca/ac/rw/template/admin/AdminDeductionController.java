package rca.ac.rw.template.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rca.ac.rw.template.deduction.dto.DeductionRequestDto;
import rca.ac.rw.template.deduction.dto.DeductionResponseDto;
import rca.ac.rw.template.deduction.service.DeductionService;

import java.util.UUID;

/**
 * Controller for Admin/Manager to manage Deduction types and percentages.
 */
@RestController
@RequestMapping("/api/v1/admin/deductions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')") // Managers or Admins can manage deductions
@Slf4j
public class AdminDeductionController {

    private final DeductionService deductionService;

    /**
     * POST /api/v1/admin/deductions : Creates a new deduction type.
     */
    @PostMapping
    public ResponseEntity<DeductionResponseDto> createDeduction(
            @Valid @RequestBody DeductionRequestDto deductionRequestDto) {
        log.info("API: Admin/Manager request to create new deduction: {}", deductionRequestDto.getCode());
        DeductionResponseDto createdDeduction = deductionService.createDeduction(deductionRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDeduction);
    }

    /**
     * GET /api/v1/admin/deductions : Retrieves all deductions, paginated.
     * Allows filtering by active status.
     */
    @GetMapping
    public ResponseEntity<Page<DeductionResponseDto>> getAllDeductions(
            @PageableDefault(size = 10, sort = "deductionName", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) Boolean isActive) {
        log.info("API: Admin/Manager request to get all deductions. IsActive filter: {}", isActive);
        Page<DeductionResponseDto> deductions = deductionService.getAllDeductions(pageable, isActive);
        return ResponseEntity.ok(deductions);
    }

    /**
     * GET /api/v1/admin/deductions/{deductionId} : Retrieves a specific deduction by its UUID.
     */
    @GetMapping("/{deductionId}")
    public ResponseEntity<DeductionResponseDto> getDeductionById(@PathVariable UUID deductionId) {
        log.info("API: Admin/Manager request to get deduction by ID: {}", deductionId);
        DeductionResponseDto deduction = deductionService.getDeductionById(deductionId);
        return ResponseEntity.ok(deduction);
    }

    /**
     * GET /api/v1/admin/deductions/by-code/{code} : Retrieves a specific deduction by its unique code.
     */
    @GetMapping("/by-code/{code}")
    public ResponseEntity<DeductionResponseDto> getDeductionByCode(@PathVariable String code) {
        log.info("API: Admin/Manager request to get deduction by code: {}", code);
        DeductionResponseDto deduction = deductionService.getDeductionByCode(code);
        return ResponseEntity.ok(deduction);
    }

    /**
     * PUT /api/v1/admin/deductions/{deductionId} : Updates an existing deduction.
     */
    @PutMapping("/{deductionId}")
    public ResponseEntity<DeductionResponseDto> updateDeduction(
            @PathVariable UUID deductionId,
            @Valid @RequestBody DeductionRequestDto deductionRequestDto) {
        log.info("API: Admin/Manager request to update deduction ID: {}", deductionId);
        DeductionResponseDto updatedDeduction = deductionService.updateDeduction(deductionId, deductionRequestDto);
        return ResponseEntity.ok(updatedDeduction);
    }

    /**
     * DELETE /api/v1/admin/deductions/{deductionId} : Deletes a deduction.
     * (Consider making this an "inactivate" PATCH endpoint instead for historical data integrity).
     */
    @DeleteMapping("/{deductionId}")
    public ResponseEntity<Void> deleteDeduction(@PathVariable UUID deductionId) {
        log.info("API: Admin/Manager request to delete deduction ID: {}", deductionId);
        deductionService.deleteDeduction(deductionId);
        return ResponseEntity.noContent().build();
    }
}