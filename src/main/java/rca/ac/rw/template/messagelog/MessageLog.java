package rca.ac.rw.template.messagelog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.audits.TimestampAudit;
import rca.ac.rw.template.employee.entity.Employee;

import java.util.UUID;

@Entity
@Table(name = "message_logs", indexes = {
        @Index(name = "idx_messagelog_employee_created", columnList = "employee_id, created_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MessageLog extends TimestampAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull(message = "Employee is required for the message log")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotBlank(message = "Message content is required")
    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    private String messageContent;

    @NotBlank(message = "Month-Year context is required")
    @Size(max = 20)
    @Column(name = "month_year_context", nullable = false, length = 20)
    private String monthYearContext;

    /**
     * Constructor for creating MessageLog instances, typically used by JPA or tests.
     * The database trigger will be doing direct INSERTs.
     */
    public MessageLog(Employee employee, String messageContent, String monthYearContext) {
        this.employee = employee;
        this.messageContent = messageContent;
        this.monthYearContext = monthYearContext;
    }
}