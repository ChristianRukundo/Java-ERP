package rca.ac.rw.template.payslip.specs;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import rca.ac.rw.template.common.enums.PayslipStatus;
import rca.ac.rw.template.employee.entity.Employee;
import rca.ac.rw.template.payslip.entity.Payslip;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // For employee ID if filtering by it

public class PayslipSpecifications {

    public static Specification<Payslip> filterPayslips(Integer year, Integer month, PayslipStatus status, UUID employeeId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (year != null) {
                predicates.add(criteriaBuilder.equal(root.get("year"), year));
            }
            if (month != null) {
                predicates.add(criteriaBuilder.equal(root.get("month"), month));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (employeeId != null) {
                Join<Payslip, Employee> employeeJoin = root.join("employee");
                predicates.add(criteriaBuilder.equal(employeeJoin.get("id"), employeeId));
            }

            // Default sort: most recent payslips first
            query.orderBy(criteriaBuilder.desc(root.get("year")), criteriaBuilder.desc(root.get("month")), criteriaBuilder.desc(root.get("createdAt")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}