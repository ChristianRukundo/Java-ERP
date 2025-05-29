package rca.ac.rw.template.employee.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rca.ac.rw.template.employee.dto.EmployeeProfileResponseDto;
import rca.ac.rw.template.employee.dto.UpdateSelfProfileRequestDto;
import rca.ac.rw.template.employee.service.EmployeeService;

@RestController
@RequestMapping("/api/v1/employee/profile")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_EMPLOYEE', 'ROLE_MANAGER', 'ROLE_ADMIN')") // Any authenticated employee type
@Slf4j
public class EmployeeProfileController {

    private final EmployeeService employeeService;

    @GetMapping("/me")
    public ResponseEntity<EmployeeProfileResponseDto> getMyProfile() {
        log.info("Fetching profile for authenticated employee.");
        EmployeeProfileResponseDto profile = employeeService.getMyProfile();
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<EmployeeProfileResponseDto> updateMyProfile(
            @Valid @RequestBody UpdateSelfProfileRequestDto updateProfileDto) {
        log.info("Attempting to update profile for authenticated employee.");
        EmployeeProfileResponseDto updatedProfile = employeeService.updateMyProfile(updateProfileDto);
        return ResponseEntity.ok(updatedProfile);
    }
}