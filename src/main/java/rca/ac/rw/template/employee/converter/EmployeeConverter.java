package rca.ac.rw.template.employee.converter;

import rca.ac.rw.template.auth.dtos.RegisterRequestDto; // For self-registration
import rca.ac.rw.template.employee.dto.AdminRegisterEmployeeRequestDto;
import rca.ac.rw.template.employee.dto.EmployeeProfileResponseDto;
import rca.ac.rw.template.employee.dto.UpdateEmployeeRequestDto;
import rca.ac.rw.template.employee.dto.UpdateSelfProfileRequestDto;
import rca.ac.rw.template.employee.entity.Employee;

public class EmployeeConverter {

    public static Employee fromSelfRegisterDto(RegisterRequestDto dto) {
        if (dto == null) return null;
        Employee employee = new Employee();
        employee.setFirstName(dto.firstName());
        employee.setLastName(dto.lastName());
        employee.setEmail(dto.email());
        employee.setMobile(dto.phoneNumber()); // Assuming DTO has phoneNumber for mobile
        employee.setNationalId(dto.nationalId());
        employee.setDateOfBirth(dto.dateOfBirth()); // Assuming DTO has dateOfBirth
        // Password, role, status, enabled, employeeCode are set by service
        return employee;
    }

    public static Employee fromAdminRegisterDto(AdminRegisterEmployeeRequestDto dto) {
        if (dto == null) return null;
        Employee employee = new Employee();
        employee.setEmployeeCode(dto.getEmployeeCode());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setMobile(dto.getMobile());
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setNationalId(dto.getNationalId());

        return employee;
    }

    public static EmployeeProfileResponseDto toProfileDto(Employee entity) {
        if (entity == null) return null;
        return new EmployeeProfileResponseDto(
                entity.getId(),
                entity.getEmployeeCode(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getMobile(),
                entity.getDateOfBirth(),
                entity.getNationalId(),
                entity.getRole(),
                entity.getStatus(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static void updateEntityFromAdminDto(UpdateEmployeeRequestDto dto, Employee employee) {
        if (dto == null || employee == null) return;
        if (dto.getFirstName() != null) employee.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) employee.setLastName(dto.getLastName());
        if (dto.getEmail() != null) employee.setEmail(dto.getEmail()); // Admin might change email
        if (dto.getMobile() != null) employee.setMobile(dto.getMobile());
        if (dto.getDateOfBirth() != null) employee.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getNationalId() != null) employee.setNationalId(dto.getNationalId()); // Admin might correct this
        if (dto.getRole() != null) employee.setRole(dto.getRole());
        if (dto.getStatus() != null) employee.setStatus(dto.getStatus());
        if (dto.getEnabled() != null) employee.setEnabled(dto.getEnabled());
        // employeeCode is typically not updated
    }

    public static void updateEntityFromSelfProfileDto(UpdateSelfProfileRequestDto dto, Employee employee) {
        if (dto == null || employee == null) return;
        if (dto.getFirstName() != null) employee.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) employee.setLastName(dto.getLastName());
        if (dto.getMobile() != null) employee.setMobile(dto.getMobile());
    }
}