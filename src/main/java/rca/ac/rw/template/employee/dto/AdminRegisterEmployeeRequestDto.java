package rca.ac.rw.template.employee.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.common.enums.EmployeeStatus;
import rca.ac.rw.template.common.enums.Role;
import rca.ac.rw.template.common.validation.ValidPassword;
import rca.ac.rw.template.common.validation.ValidRwandaId;
import rca.ac.rw.template.common.validation.ValidRwandanPhoneNumber;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRegisterEmployeeRequestDto {
    @NotBlank String employeeCode;
    @NotBlank @Size(min = 2, max = 50) String firstName;
    @NotBlank @Size(min = 2, max = 50) String lastName;
    @NotBlank @Email String email;
    @NotBlank
    @ValidRwandanPhoneNumber
    String  mobile;
    @NotNull @Past LocalDate dateOfBirth;
    @NotBlank @ValidRwandaId  String nationalId;
    @NotBlank @ValidPassword String password;
    @NotNull Role role; // Admin sets the role
    EmployeeStatus status = EmployeeStatus.ACTIVE; // Default for admin creation
    boolean enabled = true; // Default for admin creation
}