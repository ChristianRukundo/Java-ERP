package rca.ac.rw.template.employment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.ac.rw.template.common.exceptions.ResourceNotFoundException;
import rca.ac.rw.template.common.exceptions.ValidationException;
import rca.ac.rw.template.employee.entity.Employee;
import rca.ac.rw.template.employee.repository.EmployeeRepository;
import rca.ac.rw.template.employment.converter.EmploymentConverter;
import rca.ac.rw.template.employment.dto.EmploymentRequestDto;
import rca.ac.rw.template.employment.dto.EmploymentResponseDto;
import rca.ac.rw.template.employment.entity.Employment;
import rca.ac.rw.template.employment.repository.EmploymentRepository;
import rca.ac.rw.template.common.enums.EmploymentStatus; // Import

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmploymentService {

    private final EmploymentRepository employmentRepository;
    private final EmployeeRepository employeeRepository; // To fetch Employee

    /**
     * Creates a new employment record for an employee.
     * If this new record is ACTIVE, it deactivates any other existing ACTIVE employment records for the same employee.
     *
     * @param dto The employment details.
     * @return The created {@link EmploymentResponseDto}.
     */
    @Transactional
    public EmploymentResponseDto createEmployment(EmploymentRequestDto dto) {
        log.info("Creating new employment record for employee ID: {}", dto.getEmployeeId());
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "ID", dto.getEmployeeId()));

        // Business Rule: Only one ACTIVE employment record per employee.
        if (dto.getStatus() == EmploymentStatus.ACTIVE) {
            employmentRepository.findByEmployeeAndStatus(employee, EmploymentStatus.ACTIVE)
                    .ifPresent(activeEmployment -> {
                        log.info("Deactivating existing active employment (ID: {}) for employee {}", activeEmployment.getId(), employee.getEmployeeCode());
                        activeEmployment.setStatus(EmploymentStatus.INACTIVE);
                        // Consider setting an end date for the old active record if appropriate
                        // activeEmployment.setEndDate(dto.getJoiningDate().minusDays(1));
                        employmentRepository.save(activeEmployment);
                    });
        }

        Employment employment = EmploymentConverter.toEntity(dto, employee);
        Employment savedEmployment = employmentRepository.save(employment);
        log.info("Employment record ID {} created for employee {}", savedEmployment.getId(), employee.getEmployeeCode());
        return EmploymentConverter.toDto(savedEmployment);
    }

    /**
     * Updates an existing employment record.
     * Handles deactivation of other active records if this one becomes active.
     *
     * @param employmentId The ID of the employment record to update.
     * @param dto          The updated employment details.
     * @return The updated {@link EmploymentResponseDto}.
     */
    @Transactional
    public EmploymentResponseDto updateEmployment(UUID employmentId, EmploymentRequestDto dto) {
        log.info("Updating employment record ID: {}", employmentId);
        Employment employment = employmentRepository.findById(employmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Employment", "ID", employmentId));

        Employee employee = employeeRepository.findById(dto.getEmployeeId()) // Or get from employment.getEmployee() if not changing employee link
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "ID", dto.getEmployeeId()));

        if (!employment.getEmployee().getId().equals(employee.getId())) {
            throw new ValidationException("Cannot change the employee associated with an employment record. Create a new record instead.");
        }

        // Business Rule: Only one ACTIVE employment record.
        if (dto.getStatus() == EmploymentStatus.ACTIVE && employment.getStatus() != EmploymentStatus.ACTIVE) {
            employmentRepository.findByEmployeeAndStatus(employee, EmploymentStatus.ACTIVE)
                    .filter(activeEmployment -> !activeEmployment.getId().equals(employmentId)) // Exclude the current one being updated
                    .ifPresent(otherActiveEmployment -> {
                        log.info("Deactivating other active employment (ID: {}) for employee {}", otherActiveEmployment.getId(), employee.getEmployeeCode());
                        otherActiveEmployment.setStatus(EmploymentStatus.INACTIVE);
                        employmentRepository.save(otherActiveEmployment);
                    });
        }

        EmploymentConverter.updateEntityFromDto(dto, employment, employee);
        Employment updatedEmployment = employmentRepository.save(employment);
        log.info("Employment record ID {} updated for employee {}", updatedEmployment.getId(), employee.getEmployeeCode());
        return EmploymentConverter.toDto(updatedEmployment);
    }

    /**
     * Retrieves all employment records for a specific employee.
     *
     * @param employeeId The UUID of the employee.
     * @param pageable Pagination information.
     * @return Page of {@link EmploymentResponseDto}.
     */
    @Transactional(readOnly = true)
    public Page<EmploymentResponseDto> getEmploymentsByEmployeeId(UUID employeeId, Pageable pageable) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "ID", employeeId));
        log.debug("Fetching employment records for employee ID: {}", employeeId);
        // This simple find might be enough, or use specifications if more filters needed.
        // Sorting is handled by pageable.
        // Page<Employment> employmentPage = employmentRepository.findByEmployee(employee, pageable); // Need this in repo
        List<Employment> employments = employmentRepository.findByEmployeeOrderByJoiningDateDesc(employee); // Get all, then paginate in memory or use Page in repo

        // Manual pagination for now if repo returns List:
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), employments.size());
        List<EmploymentResponseDto> dtoList = employments.subList(start, end).stream()
                .map(EmploymentConverter::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, employments.size());
    }

    /**
     * Retrieves a specific employment record by its ID.
     */
    @Transactional(readOnly = true)
    public EmploymentResponseDto getEmploymentById(UUID employmentId) {
        Employment employment = employmentRepository.findById(employmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Employment", "ID", employmentId));
        return EmploymentConverter.toDto(employment);
    }

    // No delete method for Employment as per requirements; records are typically made INACTIVE.
    // If soft delete is added to Employment entity, a softDelete method could be here.
}