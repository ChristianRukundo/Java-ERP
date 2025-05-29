package rca.ac.rw.template.messagelog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import rca.ac.rw.template.employee.entity.Employee; // Corrected import
import rca.ac.rw.template.messagelog.entity.MessageLog;

import java.util.UUID;

@Repository
public interface MessageLogRepository extends JpaRepository<MessageLog, UUID>, JpaSpecificationExecutor<MessageLog> {
    // ID type matches MessageLog.id (UUID in this case)

    /**
     * Finds all message logs for a specific employee, ordered by creation time descending.
     * @param employee The employee entity.
     * @param pageable Pagination information.
     * @return A page of message logs.
     */
    Page<MessageLog> findByEmployeeOrderByCreatedAtDesc(Employee employee, Pageable pageable);

    /**
     * Finds all message logs for a specific employee ID, ordered by creation time descending.
     * @param employeeId The UUID of the employee.
     * @param pageable Pagination information.
     * @return A page of message logs.
     */
    Page<MessageLog> findByEmployee_IdOrderByCreatedAtDesc(UUID employeeId, Pageable pageable);
    // Note: Spring Data JPA can derive queries from property paths like 'employee.id'
}