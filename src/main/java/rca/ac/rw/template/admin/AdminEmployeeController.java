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
import rca.ac.rw.template.common.enums.EmployeeStatus;
import rca.ac.rw.template.common.enums.Role;
import rca.ac.rw.template.employee.dto.AdminRegisterEmployeeRequestDto;
import rca.ac.rw.template.employee.dto.EmployeeProfileResponseDto;
import rca.ac.rw.template.employee.dto.UpdateEmployeeRequestDto;
import rca.ac.rw.template.employee.service.EmployeeService;
import rca.ac.rw.template.employment.dto.EmploymentRequestDto;
import rca.ac.rw.template.employment.dto.EmploymentResponseDto;
import rca.ac.rw.template.employment.service.EmploymentService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/employees") // Endpoint for admin/manager managing employees
@RequiredArgsConstructor
@Slf4j
public class AdminEmployeeController {

    private final EmployeeService employeeService;
    private final EmploymentService employmentService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')") // Managers or Admins can add employees
    public ResponseEntity<EmployeeProfileResponseDto> registerEmployee(
            @Valid @RequestBody AdminRegisterEmployeeRequestDto registerDto) {
        log.info("Admin/Manager request to register new employee: {}", registerDto.getEmail());
        EmployeeProfileResponseDto newEmployee = employeeService.registerEmployeeByAdmin(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newEmployee);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<EmployeeProfileResponseDto>> getAllEmployees(
            @PageableDefault(size = 10, sort = "email", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role roleFilter,
            @RequestParam(required = false) EmployeeStatus statusFilter) {
        log.info("Admin/Manager request to get all employees. Search: [{}], Role: [{}], Status: [{}]",
                search, roleFilter, statusFilter);
        Page<EmployeeProfileResponseDto> employees = employeeService.getAllEmployeesAdmin(pageable, search, roleFilter, statusFilter);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{employeeId}") // Using UUID technical ID
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<EmployeeProfileResponseDto> getEmployeeById(@PathVariable UUID employeeId) {
        log.info("Admin/Manager request to get employee by ID: {}", employeeId);
        EmployeeProfileResponseDto employee = employeeService.getEmployeeByIdAdmin(employeeId);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/by-code/{employeeCode}") // Using business key
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<EmployeeProfileResponseDto> getEmployeeByCode(@PathVariable String employeeCode) {
        log.info("Admin/Manager request to get employee by code: {}", employeeCode);
        EmployeeProfileResponseDto employee = employeeService.getEmployeeByCodeAdmin(employeeCode);
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/{employeeCode}") // Update using business key employeeCode
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<EmployeeProfileResponseDto> updateEmployee(
            @PathVariable String employeeCode,
            @Valid @RequestBody UpdateEmployeeRequestDto updateDto) {
        log.info("Admin/Manager request to update employee with code: {}", employeeCode);
        EmployeeProfileResponseDto updatedEmployee = employeeService.updateEmployeeByAdmin(employeeCode, updateDto);
        return ResponseEntity.ok(updatedEmployee);
    }

    @PostMapping("/employment")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<EmploymentResponseDto> addEmploymentRecord(
            @Valid @RequestBody EmploymentRequestDto employmentRequestDto) {
        log.info("Admin/Manager request to add employment record for employee ID: {}", employmentRequestDto.getEmployeeId());
        EmploymentResponseDto newEmployment = employmentService.createEmployment(employmentRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newEmployment);
    }

    /**
     * PUT /api/v1/admin/employees/employment/{employmentId} : Admin or Manager updates an employment record.
     */
    @PutMapping("/employment/{employmentId}")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<EmploymentResponseDto> updateEmploymentRecord(
            @PathVariable UUID employmentId,
            @Valid @RequestBody EmploymentRequestDto employmentRequestDto) {
        log.info("Admin/Manager request to update employment record ID: {}", employmentId);
        EmploymentResponseDto updatedEmployment = employmentService.updateEmployment(employmentId, employmentRequestDto);
        return ResponseEntity.ok(updatedEmployment);
    }

    /**
     * GET /api/v1/admin/employees/{employeeId}/employment : Get all employment records for a specific employee.
     */
    @GetMapping("/{employeeId}/employment")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<EmploymentResponseDto>> getEmploymentRecordsForEmployee(
            @PathVariable UUID employeeId,
            @PageableDefault(size = 5, sort = "joiningDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Admin/Manager request to get employment records for employee ID: {}", employeeId);
        Page<EmploymentResponseDto> employments = employmentService.getEmploymentsByEmployeeId(employeeId, pageable);
        return ResponseEntity.ok(employments);
    }

    /**
     * GET /api/v1/admin/employees/employment/{employmentId} : Get a specific employment record by its ID.
     */
    @GetMapping("/employment/{employmentId}")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<EmploymentResponseDto> getEmploymentRecordById(@PathVariable UUID employmentId) {
        log.info("Admin/Manager request to get employment record by ID: {}", employmentId);
        EmploymentResponseDto employment = employmentService.getEmploymentById(employmentId);
        return ResponseEntity.ok(employment);
    }
}