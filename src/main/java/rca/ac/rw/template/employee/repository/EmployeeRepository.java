package rca.ac.rw.template.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rca.ac.rw.template.common.enums.EmployeeStatus;
import rca.ac.rw.template.common.enums.EmploymentStatus;
import rca.ac.rw.template.employee.entity.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<Employee> findByEmployeeCode(String employeeCode);
    boolean existsByEmployeeCode(String employeeCode);

    Optional<Employee> findByMobile(String mobile);
    boolean existsByMobile(String mobile);


    @Query("SELECT DISTINCT e FROM Employee e JOIN e.employmentRecords empr " +
            "WHERE e.status = :empStatus AND empr.status = :employmentStatus")
    List<Employee> findActiveEmployeesWithActiveEmployment(
            @Param("empStatus") EmployeeStatus empStatus,
            @Param("employmentStatus") EmploymentStatus employmentStatus
    );

    Optional<Employee> findByNationalId(String nationalId);
    boolean existsByNationalId(String nationalId);

    @Query("SELECT DISTINCT e FROM Employee e JOIN e.employmentRecords empr WHERE e.status = :empStatus AND empr.status = :employmentStatus")
    List<Employee> findByStatusAndEmploymentRecords_Status(@Param("empStatus") EmployeeStatus empStatus, @Param("employmentStatus") EmploymentStatus employmentStatus);



    // For combined uniqueness check during registration
    boolean existsByEmailOrMobileOrNationalIdOrEmployeeCode(
            String email, String mobile, String nationalId, String employeeCode
    );
}