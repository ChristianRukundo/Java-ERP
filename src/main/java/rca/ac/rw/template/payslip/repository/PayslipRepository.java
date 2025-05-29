package rca.ac.rw.template.payslip.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import rca.ac.rw.template.common.enums.PayslipStatus;
import rca.ac.rw.template.employee.entity.Employee;
import rca.ac.rw.template.payslip.entity.Payslip;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long>, JpaSpecificationExecutor<Payslip> {

    boolean existsByEmployeeAndYearAndMonth(Employee employee, int year, int month);

    Optional<Payslip> findByEmployeeAndYearAndMonth(Employee employee, int year, int month);

    Page<Payslip> findByEmployeeOrderByYearDescMonthDesc(Employee employee, Pageable pageable);

    Page<Payslip> findByYearAndMonthAndStatus(int year, int month, PayslipStatus status, Pageable pageable);
    Page<Payslip> findByYearAndMonth(int year, int month, Pageable pageable);

    List<Payslip> findByYearAndMonthAndStatus(int year, int month, PayslipStatus status); // For batch updates
}