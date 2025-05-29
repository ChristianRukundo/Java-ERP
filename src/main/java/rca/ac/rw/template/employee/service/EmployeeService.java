package rca.ac.rw.template.employee.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.ac.rw.template.auth.dtos.RegisterRequestDto; // For self-registration
import rca.ac.rw.template.common.exceptions.BadRequestException;
import rca.ac.rw.template.common.exceptions.ResourceNotFoundException;
import rca.ac.rw.template.common.exceptions.UnauthenticatedException;
import rca.ac.rw.template.common.exceptions.ValidationException;
import rca.ac.rw.template.common.enums.EmployeeStatus;
import rca.ac.rw.template.common.enums.Role;
import rca.ac.rw.template.employee.converter.EmployeeConverter;
import rca.ac.rw.template.employee.dto.AdminRegisterEmployeeRequestDto;
import rca.ac.rw.template.employee.dto.EmployeeProfileResponseDto;
import rca.ac.rw.template.employee.dto.UpdateEmployeeRequestDto;
import rca.ac.rw.template.employee.dto.UpdateSelfProfileRequestDto;
import rca.ac.rw.template.employee.entity.Employee;
import rca.ac.rw.template.employee.repository.EmployeeRepository;
import rca.ac.rw.template.employee.specs.EmployeeSpecifications; // We'll create this

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Handles employee self-registration.
     * Creates an Employee with a system-generated employeeCode, default ROLE_EMPLOYEE,
     * PENDING status, and disabled.
     *
     * @param dto The {@link RegisterRequestDto} from auth module.
     * @return {@link EmployeeProfileResponseDto} of the newly registered employee.
     */
    @Transactional
    public EmployeeProfileResponseDto selfRegisterEmployee(RegisterRequestDto dto) {
        log.info("Processing self-registration for employee with email: {}", dto.email());

        if (employeeRepository.existsByEmailOrMobileOrNationalIdOrEmployeeCode(
                dto.email(), dto.phoneNumber(), dto.nationalId(), null /* employeeCode not known yet */)) {
            throw new BadRequestException("Employee with provided email, phone number, or national ID already exists.");
        }

        Employee employee = EmployeeConverter.fromSelfRegisterDto(dto);
        employee.setEmployeeCode(generateUniqueEmployeeCode()); // Generate business key
        employee.setPassword(passwordEncoder.encode(dto.password()));
        employee.setRole(Role.ROLE_EMPLOYEE);
        employee.setStatus(EmployeeStatus.PENDING); // Requires OTP verification/admin approval
        employee.setEnabled(false);

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee self-registered successfully. Email: {}, Employee Code: {}",
                savedEmployee.getEmail(), savedEmployee.getEmployeeCode());
        return EmployeeConverter.toProfileDto(savedEmployee);
    }

    /**
     * Admin registers a new employee with full details.
     */
    @Transactional
    public EmployeeProfileResponseDto registerEmployeeByAdmin(AdminRegisterEmployeeRequestDto dto) {
        log.info("Admin registering new employee. Email: {}, Employee Code: {}", dto.getEmail(), dto.getEmployeeCode());
        if (employeeRepository.existsByEmailOrMobileOrNationalIdOrEmployeeCode(
                dto.getEmail(), dto.getMobile(), dto.getNationalId(), dto.getEmployeeCode())) {
            throw new BadRequestException("Employee with provided email, mobile, national ID, or employee code already exists.");
        }

        Employee employee = EmployeeConverter.fromAdminRegisterDto(dto);
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        // Role, Status, Enabled are set from DTO by converter or here if defaults differ
        if (dto.getRole() == null) employee.setRole(Role.ROLE_EMPLOYEE); else employee.setRole(dto.getRole());
        if (dto.getStatus() == null) employee.setStatus(EmployeeStatus.ACTIVE); else employee.setStatus(dto.getStatus());
        employee.setEnabled(dto.isEnabled());


        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee registered by admin. Email: {}, Employee Code: {}",
                savedEmployee.getEmail(), savedEmployee.getEmployeeCode());
        return EmployeeConverter.toProfileDto(savedEmployee);
    }

    /**
     * Retrieves the currently authenticated employee entity.
     */
    @Transactional(readOnly = true)
    public Employee getAuthenticatedEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("getAuthenticatedEmployee called but no valid authentication found.");
            throw new UnauthenticatedException("User is not authenticated.");
        }

        // The 'name' from the Authentication object (principal) should be the email,
        // as set by CustomUserDetailsService and a correctly configured JwtAuthenticationFilter.
        String email = authentication.getName();
        log.debug("Fetching authenticated employee by email (from security principal name): {}", email);

        if (email == null || email.isEmpty()) {
            log.error("Authenticated principal name (expected email) is null or empty.");
            throw new UnauthenticatedException("Invalid authenticated principal identifier.");
        }

        return employeeRepository.findByEmail(email) // Find by email
                .orElseThrow(() -> {
                    log.warn("Authenticated employee with email {} (from security principal) not found in database.", email);
                    // This could mean the JWT is valid but the user was deleted, or an inconsistency.
                    return new ResourceNotFoundException("Authenticated Employee Profile", "email", email);
                });
    }
    /**
     * Gets the profile of the currently authenticated employee.
     */
    @Transactional(readOnly = true)
    public EmployeeProfileResponseDto getMyProfile() {
        Employee employee = getAuthenticatedEmployee();
        return EmployeeConverter.toProfileDto(employee);
    }

    /**
     * Allows authenticated employee to update their own limited profile information.
     */
    @Transactional
    public EmployeeProfileResponseDto updateMyProfile(UpdateSelfProfileRequestDto dto) {
        Employee employee = getAuthenticatedEmployee();
        log.info("Employee {} (Code: {}) updating their profile.", employee.getEmail(), employee.getEmployeeCode());

        // Uniqueness check for mobile if changed
        if (dto.getMobile() != null && !dto.getMobile().equals(employee.getMobile())) {
            employeeRepository.findByMobile(dto.getMobile())
                    .filter(e -> !e.getId().equals(employee.getId()))
                    .ifPresent(ex -> { throw new ValidationException("Mobile number already in use."); });
        }
        EmployeeConverter.updateEntityFromSelfProfileDto(dto, employee);
        Employee updatedEmployee = employeeRepository.save(employee);
        return EmployeeConverter.toProfileDto(updatedEmployee);
    }


    /**
     * Admin retrieves a paginated list of all employees.
     */
    @Transactional(readOnly = true)
    public Page<EmployeeProfileResponseDto> getAllEmployeesAdmin(Pageable pageable, String searchTerm, Role roleFilter, EmployeeStatus statusFilter) {
        log.debug("Admin fetching all employees. Search: [{}], Role: [{}], Status: [{}]", searchTerm, roleFilter, statusFilter);
        Specification<Employee> spec = EmployeeSpecifications.filterEmployees(searchTerm, roleFilter, statusFilter, false); // false = exclude deleted
        Page<Employee> employeePage = employeeRepository.findAll(spec, pageable);
        List<EmployeeProfileResponseDto> dtos = employeePage.getContent().stream()
                .map(EmployeeConverter::toProfileDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, employeePage.getTotalElements());
    }

    /**
     * Admin retrieves a specific employee by their technical UUID.
     */
    @Transactional(readOnly = true)
    public EmployeeProfileResponseDto getEmployeeByIdAdmin(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "ID", employeeId));
        return EmployeeConverter.toProfileDto(employee);
    }

    /**
     * Admin retrieves a specific employee by their business employeeCode.
     */
    @Transactional(readOnly = true)
    public EmployeeProfileResponseDto getEmployeeByCodeAdmin(String employeeCode) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "Code", employeeCode));
        return EmployeeConverter.toProfileDto(employee);
    }

    /**
     * Admin updates an employee's details.
     */
    @Transactional
    public EmployeeProfileResponseDto updateEmployeeByAdmin(String employeeCode, UpdateEmployeeRequestDto dto) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "Code", employeeCode));
        log.info("Admin updating employee Code: {}", employeeCode);

        // Uniqueness checks for email, mobile, nationalId if they are being changed
        if (dto.getEmail() != null && !dto.getEmail().equals(employee.getEmail())) {
            if(employeeRepository.existsByEmail(dto.getEmail())) throw new ValidationException("Email already in use.");
        }
        if (dto.getMobile() != null && !dto.getMobile().equals(employee.getMobile())) {
            if(employeeRepository.existsByMobile(dto.getMobile())) throw new ValidationException("Mobile number already in use.");
        }
        if (dto.getNationalId() != null && !dto.getNationalId().equals(employee.getNationalId())) {
            if(employeeRepository.existsByNationalId(dto.getNationalId())) throw new ValidationException("National ID already in use.");
        }

        EmployeeConverter.updateEntityFromAdminDto(dto, employee);
        // The syncEnabledWithStatus method in Employee entity will handle 'enabled' based on 'status'
        Employee updatedEmployee = employeeRepository.save(employee);
        return EmployeeConverter.toProfileDto(updatedEmployee);
    }


    // --- Methods for AuthController ---
    @Transactional
    public Employee activateEmployeeAccount(String email) {
        log.info("Activating employee account for email: {}", email);
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "email for activation", email));
        if (employee.isEnabled() && employee.getStatus() == EmployeeStatus.ACTIVE) {
            log.warn("Employee account {} is already active.", email);
            return employee;
        }
        employee.setStatus(EmployeeStatus.ACTIVE); // This will also set enabled=true via @PreUpdate
        return employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public Employee findEmployeeByEmailForPasswordReset(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "email for password reset", email));
    }

    @Transactional
    public void setEmployeeStatusToReset(String email) {
        Employee employee = findEmployeeByEmailForPasswordReset(email);
        employee.setStatus(EmployeeStatus.PENDING); // Or a specific RESET status
        // employee.setEnabled(false); // If reset means they can't log in until password changed
        employeeRepository.save(employee);
        log.info("Status for employee {} set to PENDING for password reset.", email);
    }

    @Transactional
    public void resetEmployeePassword(String email, String newPassword) {
        Employee employee = findEmployeeByEmailForPasswordReset(email);
        employee.setPassword(passwordEncoder.encode(newPassword));
        employee.setStatus(EmployeeStatus.ACTIVE); // Re-activate after password reset
        // employee.setEnabled(true); // Handled by status change via @PreUpdate
        employeeRepository.save(employee);
        log.info("Password reset successfully for employee {}.", email);
    }


    private String generateUniqueEmployeeCode() {
        // Simple 8-digit numeric code, ensure uniqueness
        String code;
        do {
            code = String.format("EMP%05d", ThreadLocalRandom.current().nextInt(1, 100000));
        } while (employeeRepository.existsByEmployeeCode(code));
        return code;
    }
}