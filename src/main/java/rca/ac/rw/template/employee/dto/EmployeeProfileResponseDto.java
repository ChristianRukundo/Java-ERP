package rca.ac.rw.template.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.common.enums.EmployeeStatus;
import rca.ac.rw.template.common.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProfileResponseDto {
    private UUID id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private LocalDate dateOfBirth;
    private String nationalId;
    private Role role;
    private EmployeeStatus status;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}