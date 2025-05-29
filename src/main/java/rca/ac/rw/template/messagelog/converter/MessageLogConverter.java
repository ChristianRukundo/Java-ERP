package rca.ac.rw.template.messagelog.converter;

import rca.ac.rw.template.employee.dto.EmployeeMiniDto;
import rca.ac.rw.template.employee.entity.Employee;
import rca.ac.rw.template.messagelog.dto.MessageLogResponseDto;
import rca.ac.rw.template.messagelog.entity.MessageLog;

public class MessageLogConverter {

    public static MessageLogResponseDto toDto(MessageLog entity) {
        if (entity == null) return null;

        EmployeeMiniDto employeeDto = null;
        if (entity.getEmployee() != null) {
            Employee emp = entity.getEmployee();
            employeeDto = new EmployeeMiniDto(emp.getId(), emp.getEmployeeCode(), emp.getFirstName(), emp.getLastName(), emp.getEmail());
        }

        return new MessageLogResponseDto(
                entity.getId(),
                employeeDto,
                entity.getMessageContent(),
                entity.getMonthYearContext(),
                entity.getCreatedAt()
        );
    }
}