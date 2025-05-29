package rca.ac.rw.template.payslip.converter;

import rca.ac.rw.template.employee.dto.EmployeeMiniDto;
import rca.ac.rw.template.employee.entity.Employee;
import rca.ac.rw.template.payslip.dto.PayslipResponseDto;
import rca.ac.rw.template.payslip.entity.Payslip;

public class PayslipConverter {

    public static PayslipResponseDto toDto(Payslip entity) {
        if (entity == null) return null;

        EmployeeMiniDto empDto = null;
        if (entity.getEmployee() != null) {
            Employee emp = entity.getEmployee();
            empDto = new EmployeeMiniDto(emp.getId(), emp.getEmployeeCode(), emp.getFirstName(), emp.getLastName(), emp.getEmail());
        }

        return new PayslipResponseDto(
                entity.getId(),
                empDto,
                entity.getYear(),
                entity.getMonth(),
                entity.getBaseSalarySnapshot(),
                entity.getGrossSalary(),
                entity.getNetSalary(),
                entity.getHouseAmount(),
                entity.getTransportAmount(),
                entity.getEmployeeTaxedAmount(),
                entity.getPensionAmount(),
                entity.getMedicalInsuranceAmount(),
                entity.getOtherTaxedAmount(),
                entity.getStatus(),
                entity.getCreatedAt(), // generatedAt
                entity.getUpdatedAt()
        );
    }
}