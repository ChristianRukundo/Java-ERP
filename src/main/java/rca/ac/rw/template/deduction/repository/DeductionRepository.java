package rca.ac.rw.template.deduction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import rca.ac.rw.template.deduction.entity.Deduction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeductionRepository extends JpaRepository<Deduction, UUID>, JpaSpecificationExecutor<Deduction> {

    Optional<Deduction> findByCode(String code);
    boolean existsByCode(String code);

    Optional<Deduction> findByDeductionName(String deductionName);
    boolean existsByDeductionName(String deductionName);

    List<Deduction> findByIsActiveTrue();
    Page<Deduction> findByIsActive(boolean isActive, Pageable pageable);
}