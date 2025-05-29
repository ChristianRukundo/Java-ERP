package rca.ac.rw.template.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.ac.rw.template.common.enums.EmployeeStatus;
import rca.ac.rw.template.common.enums.Role;
import rca.ac.rw.template.employee.entity.Employee;
import rca.ac.rw.template.employee.repository.EmployeeRepository;

import java.util.Collections;
import java.util.Set;

/**
 * Service to load Employee-specific user details for Spring Security.
 * It uses the employee's email as the username.
 */
@Service("customUserDetailsService")
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    /**
     * Loads the employee by their email address (which serves as the username).
     * This method is used by Spring Security during the authentication process.
     *
     * @param email The email address (username) of the employee to load.
     * @return UserDetails object containing employee information if found and active.
     * @throws UsernameNotFoundException if the employee with the given email is not found,
     *                                   or if their account is not active/enabled.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Attempting to load employee by email for authentication: {}", email);

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: Employee not found with email: {}", email);
                    return new UsernameNotFoundException("Invalid email or password.");
                });

        if (employee.getStatus() != EmployeeStatus.ACTIVE || !employee.isEnabled()) {
            log.warn("Authentication failed: Employee account for {} is not active or enabled. Status: {}, Enabled: {}",
                    email, employee.getStatus(), employee.isEnabled());
            throw new UsernameNotFoundException("Employee account is not active or enabled.");
        }

        Set<GrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority(employee.getRole().name())
        );

        log.info("Employee '{}' found and successfully loaded for authentication with role: {}", email, employee.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                employee.getEmail(),
                employee.getPassword(),
                employee.isEnabled(),
                true,
                true,
                true,
                authorities
        );
    }
}