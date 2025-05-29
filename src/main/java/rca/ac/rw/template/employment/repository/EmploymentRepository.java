package rca.ac.rw.template.employment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rca.ac.rw.template.common.enums.EmploymentStatus;
import rca.ac.rw.template.employee.entity.Employee;
import rca.ac.rw.template.employment.entity.Employment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmploymentRepository extends JpaRepository<Employment, UUID>, JpaSpecificationExecutor<Employment> {

    List<Employment> findByEmployee(Employee employee);
    List<Employment> findByEmployeeOrderByJoiningDateDesc(Employee employee);

    Optional<Employment> findByEmployeeAndStatus(Employee employee, EmploymentStatus status);

    // Find the current active employment record for an employee
    @Query("SELECT e FROM Employment e WHERE e.employee = :employee AND e.status = 'ACTIVE' ORDER BY e.joiningDate DESC")
    Optional<Employment> findCurrentActiveEmployment(Employee employee);
    // Or if using a Pageable to get the latest one:
    // Optional<Employment> findFirstByEmployeeAndStatusOrderByJoiningDateDesc(Employee employee, EmploymentStatus status);

    boolean existsByEmployeeAndStatus(Employee employee, EmploymentStatus status);
}