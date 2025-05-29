package rca.ac.rw.template.employment.converter;

import rca.ac.rw.template.employment.dto.EmploymentRequestDto;
import rca.ac.rw.template.employment.dto.EmploymentResponseDto;
import rca.ac.rw.template.employment.entity.Employment;
import rca.ac.rw.template.employee.entity.Employee; // Import Employee

public class EmploymentConverter {

    public static Employment toEntity(EmploymentRequestDto dto, Employee employee) {
        if (dto == null || employee == null) return null;
        Employment employment = new Employment();
        employment.setEmployee(employee); // Link to the fetched Employee entity
        employment.setDepartment(dto.getDepartment());
        employment.setPosition(dto.getPosition());
        employment.setBaseSalary(dto.getBaseSalary());
        employment.setStatus(dto.getStatus());
        employment.setJoiningDate(dto.getJoiningDate());
        employment.setEndDate(dto.getEndDate());
        return employment;
    }

    public static EmploymentResponseDto toDto(Employment entity) {
        if (entity == null) return null;
        Employee emp = entity.getEmployee();
        return new EmploymentResponseDto(
                entity.getId(),
                emp != null ? emp.getId() : null,
                emp != null ? emp.getEmployeeCode() : null,
                emp != null ? emp.getFirstName() + " " + emp.getLastName() : null,
                entity.getDepartment(),
                entity.getPosition(),
                entity.getBaseSalary(),
                entity.getStatus(),
                entity.getJoiningDate(),
                entity.getEndDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static void updateEntityFromDto(EmploymentRequestDto dto, Employment employment, Employee employee) {
        if (dto == null || employment == null || employee == null) return;

        employment.setEmployee(employee);
        if (dto.getDepartment() != null) employment.setDepartment(dto.getDepartment());
        if (dto.getPosition() != null) employment.setPosition(dto.getPosition());
        if (dto.getBaseSalary() != null) employment.setBaseSalary(dto.getBaseSalary());
        if (dto.getStatus() != null) employment.setStatus(dto.getStatus());
        if (dto.getJoiningDate() != null) employment.setJoiningDate(dto.getJoiningDate());
        employment.setEndDate(dto.getEndDate());
    }
}