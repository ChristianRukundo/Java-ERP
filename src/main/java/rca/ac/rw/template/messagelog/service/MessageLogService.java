package rca.ac.rw.template.messagelog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.ac.rw.template.employee.entity.Employee; // Corrected import
import rca.ac.rw.template.employee.repository.EmployeeRepository; // Corrected import
import rca.ac.rw.template.common.exceptions.ResourceNotFoundException;
import rca.ac.rw.template.messagelog.converter.MessageLogConverter;
import rca.ac.rw.template.messagelog.dto.MessageLogResponseDto;
import rca.ac.rw.template.messagelog.entity.MessageLog;
import rca.ac.rw.template.messagelog.repository.MessageLogRepository;
import rca.ac.rw.template.messagelog.specs.MessageLogSpecifications;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageLogService {

    private final MessageLogRepository messageLogRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Retrieves all message logs, paginated and optionally filtered.
     *
     * @param pageable   Pagination information (default sort should be by loggedAt descending).
     * @param employeeId Optional UUID of the employee to filter messages for.
     * @param searchTerm Optional term to search within message content.
     * @return Page of {@link MessageLogResponseDto}.
     */
    @Transactional(readOnly = true)
    public Page<MessageLogResponseDto> getAllMessageLogsAdmin(Pageable pageable, UUID employeeId, String searchTerm) {
        log.debug("Admin fetching message logs. EmployeeId: {}, SearchTerm: '{}'", employeeId, searchTerm);

        Employee employeeFilter = null;
        if (employeeId != null) {
            employeeFilter = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", "ID for message log filter", employeeId.toString()));
        }

        Specification<MessageLog> spec = MessageLogSpecifications.filterLogs(employeeFilter, searchTerm);
        Page<MessageLog> logPage = messageLogRepository.findAll(spec, pageable);

        List<MessageLogResponseDto> dtos = logPage.getContent().stream()
                .map(MessageLogConverter::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, logPage.getTotalElements());
    }
}