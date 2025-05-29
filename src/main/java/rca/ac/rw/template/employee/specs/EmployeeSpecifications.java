package rca.ac.rw.template.employee.specs;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import rca.ac.rw.template.common.enums.EmployeeStatus;
import rca.ac.rw.template.common.enums.Role;
import rca.ac.rw.template.employee.entity.Employee;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecifications {

    public static Specification<Employee> filterEmployees(String searchTerm, Role role, EmployeeStatus status, boolean includeDisabled) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(searchTerm)) {
                String lowerSearch = searchTerm.toLowerCase().trim();
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + lowerSearch + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + lowerSearch + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + lowerSearch + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("mobile")), "%" + lowerSearch + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nationalId")), "%" + lowerSearch + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("employeeCode")), "%" + lowerSearch + "%")
                ));
            }
            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Assuming Employee entity itself doesn't have @Where(clause="deleted=false")
            // and we don't have a 'deleted' flag yet. If we add soft delete, this needs adjustment.
            // For now, 'includeDisabled' is more about the 'status' field.
            // If status is ACTIVE, it implies enabled. If DISABLED, it implies not enabled.

            // If you had a separate 'deleted' field and @Where was removed:
            // if (!includeDeleted) { // Assuming a parameter 'includeDeleted'
            //    predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
            // }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}