package rca.ac.rw.template.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.common.enums.EmployeeStatus;
import rca.ac.rw.template.common.enums.Role;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequestDto { // For admin updates
    @Size(min = 2, max = 50) String firstName;
    @Size(min = 2, max = 50) String lastName;
    @Email String email; // Admin might change email (with re-verification flow usually)
    @Pattern(regexp = "^(072|073|078|079)\\d{7}$") String mobile; // Or phoneNumber
    @Past LocalDate dateOfBirth;
    @Size(min = 16, max = 16) String nationalId;
    Role role;
    EmployeeStatus status;
    Boolean enabled;
    // Employee code is usually not updatable
    // Password change is a separate flow
}