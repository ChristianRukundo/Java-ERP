package rca.ac.rw.template.messagelog.specs;

import jakarta.persistence.criteria.Join; // For joining
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import rca.ac.rw.template.employee.entity.Employee; // Corrected import
import rca.ac.rw.template.messagelog.entity.MessageLog;

import java.util.ArrayList;
import java.util.List;

public class MessageLogSpecifications {

    public static Specification<MessageLog> filterLogs(Employee employee, String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (employee != null) {
                // Assuming MessageLog has 'employee' field which is an Employee entity
                predicates.add(criteriaBuilder.equal(root.get("employee"), employee));
            }

            if (StringUtils.hasText(searchTerm)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("messageContent")), "%" + searchTerm.toLowerCase() + "%"));
            }

            // Default sort by creation date (loggedAt) descending if Pageable doesn't specify sort
            if (query.getOrderList().isEmpty()) {
                query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}