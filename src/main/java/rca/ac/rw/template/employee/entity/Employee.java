package rca.ac.rw.template.employee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import rca.ac.rw.template.audits.InitiatorAudit;
import rca.ac.rw.template.common.enums.EmployeeStatus;
import rca.ac.rw.template.common.enums.Role;
import rca.ac.rw.template.common.validation.ValidPassword;
import rca.ac.rw.template.common.validation.ValidRwandaId;
import rca.ac.rw.template.common.validation.ValidRwandanPhoneNumber;
import rca.ac.rw.template.employment.entity.Employment;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employee_code_unique", columnList = "employeeCode", unique = true),
        @Index(name = "idx_employee_email_unique", columnList = "email", unique = true),
        @Index(name = "idx_employee_mobile_unique", columnList = "mobile", unique = true),
        @Index(name = "idx_employee_national_id_unique", columnList = "nationalId", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = {"password"})
@EqualsAndHashCode(callSuper = true)
public class Employee extends InitiatorAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank(message = "Employee code is required")
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9\\-]*$", message = "Employee code can only contain alphanumeric characters and hyphens")
    @Column(name = "employee_code", nullable = false, unique = true, length = 50)
    private String employeeCode;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;


    @NotBlank(message = "Mobile number is required")
@ValidRwandanPhoneNumber
    @Column(nullable = false, unique = true, length = 20)
    private String mobile;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotNull(message = "Employee status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeStatus status = EmployeeStatus.PENDING;

    @Column(nullable = false)
    private boolean enabled = false;

    @NotBlank(message = "National ID is required")
    private String nationalId;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Employment> employmentRecords = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void syncEnabledWithStatus() {
        if (this.status == EmployeeStatus.ACTIVE) {
            this.enabled = true;
        } else {
            this.enabled = false;
        }
    }
}