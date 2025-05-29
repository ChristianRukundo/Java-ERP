package rca.ac.rw.template.user; // Assuming user package

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email); // @Where on User handles deleted=false
    boolean existsByEmail(String email);


    Optional<User> findByNationalId(String nationalId);
    boolean existsByNationalId(String nationalId);

    Optional<User> findByPhoneNumber(String phoneNumber);


    boolean existsByEmailOrPhoneNumberOrNationalId(String email, String phoneNumber, String nationalId);


}