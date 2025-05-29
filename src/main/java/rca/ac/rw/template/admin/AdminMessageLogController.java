package rca.ac.rw.template.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // Import Sort
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rca.ac.rw.template.messagelog.dto.MessageLogResponseDto;
import rca.ac.rw.template.messagelog.service.MessageLogService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/message-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')") // Only Admins can view all message logs
@Slf4j
public class AdminMessageLogController {

    private final MessageLogService messageLogService;

    /**
     * GET /api/v1/admin/message-logs : Admin retrieves all message logs.
     * Supports pagination and filtering by employee ID or search term in message content.
     * Default sort is by loggedAt (createdAt) descending.
     */
    @GetMapping
    public ResponseEntity<Page<MessageLogResponseDto>> getAllMessageLogs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(name = "employee", required = false) UUID employeeId, // Changed param name for clarity
            @RequestParam(required = false) String search) {
        log.info("Admin request to fetch message logs. EmployeeId: {}, Search: '{}'", employeeId, search);
        Page<MessageLogResponseDto> logs = messageLogService.getAllMessageLogsAdmin(pageable, employeeId, search);
        return ResponseEntity.ok(logs);
    }
}