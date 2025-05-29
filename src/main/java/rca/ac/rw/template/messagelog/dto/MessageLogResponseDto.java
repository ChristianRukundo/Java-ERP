package rca.ac.rw.template.messagelog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.employee.dto.EmployeeMiniDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageLogResponseDto {
    private UUID id;
    private EmployeeMiniDto employee;
    private String messageContent;
    private String monthYearContext;
    private LocalDateTime loggedAt;
}