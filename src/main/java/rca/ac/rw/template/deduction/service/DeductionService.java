package rca.ac.rw.template.deduction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // For potential future specs
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.ac.rw.template.common.exceptions.BadRequestException;
import rca.ac.rw.template.common.exceptions.ResourceNotFoundException;
import rca.ac.rw.template.deduction.converter.DeductionConverter;
import rca.ac.rw.template.deduction.dto.DeductionRequestDto;
import rca.ac.rw.template.deduction.dto.DeductionResponseDto;
import rca.ac.rw.template.deduction.entity.Deduction;
import rca.ac.rw.template.deduction.repository.DeductionRepository;
// import rca.ac.rw.template.deduction.specs.DeductionSpecifications; // If using specifications

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeductionService {

    private final DeductionRepository deductionRepository;

    /**
     * Creates a new deduction type.
     *
     * @param dto The request DTO containing deduction details.
     * @return The created {@link DeductionResponseDto}.
     * @throws BadRequestException if a deduction with the same code or name already exists.
     */
    @Transactional
    public DeductionResponseDto createDeduction(DeductionRequestDto dto) {
        log.info("Creating new deduction with code: {} and name: {}", dto.getCode(), dto.getDeductionName());
        if (deductionRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Deduction with code '" + dto.getCode() + "' already exists.");
        }
        if (deductionRepository.existsByDeductionName(dto.getDeductionName())) {
            throw new BadRequestException("Deduction with name '" + dto.getDeductionName() + "' already exists.");
        }

        Deduction deduction = DeductionConverter.toEntity(dto);
        Deduction savedDeduction = deductionRepository.save(deduction);
        log.info("Deduction created successfully with ID: {}", savedDeduction.getId());
        return DeductionConverter.toDto(savedDeduction);
    }

    /**
     * Updates an existing deduction type.
     *
     * @param deductionId The UUID of the deduction to update.
     * @param dto         The request DTO with updated details.
     * @return The updated {@link DeductionResponseDto}.
     * @throws ResourceNotFoundException if the deduction is not found.
     * @throws BadRequestException if the new name (if changed) conflicts with another existing deduction.
     */
    @Transactional
    public DeductionResponseDto updateDeduction(UUID deductionId, DeductionRequestDto dto) {
        log.info("Updating deduction with ID: {}", deductionId);
        Deduction existingDeduction = deductionRepository.findById(deductionId)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction", "ID", deductionId));

        // Check for name conflict if name is being changed
        if (dto.getDeductionName() != null && !dto.getDeductionName().equalsIgnoreCase(existingDeduction.getDeductionName())) {
            deductionRepository.findByDeductionName(dto.getDeductionName())
                    .filter(d -> !d.getId().equals(deductionId)) // Ensure it's not the same deduction
                    .ifPresent(d -> {
                        throw new BadRequestException("Deduction with name '" + dto.getDeductionName() + "' already exists.");
                    });
        }
        // Code is typically not updatable as it's a business key. If it were, similar uniqueness check needed.

        DeductionConverter.updateEntityFromDto(dto, existingDeduction);
        Deduction updatedDeduction = deductionRepository.save(existingDeduction);
        log.info("Deduction ID {} updated successfully.", updatedDeduction.getId());
        return DeductionConverter.toDto(updatedDeduction);
    }

    /**
     * Retrieves a specific deduction by its ID.
     *
     * @param deductionId The UUID of the deduction.
     * @return {@link DeductionResponseDto}.
     */
    @Transactional(readOnly = true)
    public DeductionResponseDto getDeductionById(UUID deductionId) {
        Deduction deduction = deductionRepository.findById(deductionId)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction", "ID", deductionId));
        return DeductionConverter.toDto(deduction);
    }

    /**
     * Retrieves a specific deduction by its unique code.
     *
     * @param code The unique code of the deduction.
     * @return {@link DeductionResponseDto}.
     */
    @Transactional(readOnly = true)
    public DeductionResponseDto getDeductionByCode(String code) {
        Deduction deduction = deductionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Deduction", "code", code));
        return DeductionConverter.toDto(deduction);
    }

    /**
     * Retrieves all deductions, paginated.
     *
     * @param pageable Pagination information.
     * @param isActiveFilter Optional filter to get only active or inactive deductions.
     * @return Page of {@link DeductionResponseDto}.
     */
    @Transactional(readOnly = true)
    public Page<DeductionResponseDto> getAllDeductions(Pageable pageable, Boolean isActiveFilter) {
        log.debug("Fetching all deductions. IsActive filter: {}", isActiveFilter);
        Page<Deduction> deductionPage;
        if (isActiveFilter != null) {
            // Specification<Deduction> spec = DeductionSpecifications.filterByActiveStatus(isActiveFilter);
            // deductionPage = deductionRepository.findAll(spec, pageable);
            // For a simple boolean filter, a direct repository method is often cleaner if no other criteria
            deductionPage = deductionRepository.findByIsActive(isActiveFilter, pageable); // Add this to repo
        } else {
            deductionPage = deductionRepository.findAll(pageable);
        }

        List<DeductionResponseDto> dtos = deductionPage.getContent().stream()
                .map(DeductionConverter::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, deductionPage.getTotalElements());
    }

    /**
     * Deletes a deduction by its ID.
     * Consider if this should be a soft delete instead. For master data like deductions,
     * making them inactive might be preferable to hard deletion if they've been used in past payrolls.
     * For this implementation, it's a hard delete.
     *
     * @param deductionId The UUID of the deduction to delete.
     */
    @Transactional
    public void deleteDeduction(UUID deductionId) {
        log.info("Deleting deduction with ID: {}", deductionId);
        if (!deductionRepository.existsById(deductionId)) {
            throw new ResourceNotFoundException("Deduction", "ID", deductionId);
        }
        // Add checks here if this deduction is linked to any historical payslips to prevent deletion
        // For now, direct delete.
        deductionRepository.deleteById(deductionId);
        log.info("Deduction ID {} deleted successfully.", deductionId);
    }

    /**
     * Retrieves all active deductions. Used by PayrollService.
     * @return List of active {@link Deduction} entities.
     */
    @Transactional(readOnly = true)
    public List<Deduction> getAllActiveDeductionsForPayroll() {
        return deductionRepository.findByIsActiveTrue();
    }


}