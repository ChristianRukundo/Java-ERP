package rca.ac.rw.template.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rca.ac.rw.template.auth.dtos.LoginRequestDto;
import rca.ac.rw.template.auth.dtos.LoginResponse;
import rca.ac.rw.template.common.enums.EmployeeStatus;
import rca.ac.rw.template.common.exceptions.UnauthenticatedException;
import rca.ac.rw.template.employee.entity.Employee;
import rca.ac.rw.template.employee.repository.EmployeeRepository;

/**
 * Service responsible for handling authentication logic, primarily login,
 * for the ERP system using Employee entities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecureFlag;

    /**
     * Retrieves the currently authenticated Employee entity.
     * The principal in the security context is expected to be the Employee's email.
     *
     * @return The authenticated {@link Employee} entity.
     * @throws UnauthenticatedException if no user is authenticated or employee record not found.
     */
    public Employee getAuthenticatedEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Attempt to get current employee when no user is authenticated.");
            throw new UnauthenticatedException("User is not authenticated.");
        }

        String email = authentication.getName();
        log.debug("Fetching authenticated employee from repository by email: {}", email);
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("CRITICAL: Authenticated employee with email {} not found in repository. Authentication principal might be stale or incorrect.", email);
                    return new UnauthenticatedException("Authenticated employee record not found. Please log in again.");
                });
    }

    /**
     * Authenticates an employee using their email and password.
     * If successful, generates JWT access and refresh tokens.
     *
     * @param loginRequest DTO containing the login credentials (email, password).
     * @param response     HttpServletResponse to set the refresh token cookie.
     * @return {@link LoginResponse} containing the access token.
     * @throws UsernameNotFoundException if the employee is not found or credentials are bad (via AuthenticationManager).
     * @throws UnauthenticatedException  if the account is not active or enabled.
     */
    public LoginResponse login(LoginRequestDto loginRequest, HttpServletResponse response) {
        log.info("Login attempt with email: {}", loginRequest.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );
        log.info("Authentication successful for email: {}", loginRequest.email());

        String authenticatedEmployeeEmail = authentication.getName();

        Employee employee = employeeRepository.findByEmail(authenticatedEmployeeEmail)
                .orElseThrow(() -> {
                    log.error("CRITICAL: Employee {} authenticated but not found in repository for token generation.", authenticatedEmployeeEmail);
                    return new UsernameNotFoundException("Authenticated employee data not found after successful authentication: " + authenticatedEmployeeEmail);
                });

        if (employee.getStatus() != EmployeeStatus.ACTIVE || !employee.isEnabled()) {
            log.warn("Login attempt for non-active/disabled employee account: {}. Status: {}, Enabled: {}",
                    employee.getEmail(), employee.getStatus(), employee.isEnabled());
            throw new UnauthenticatedException("Employee account is not active or enabled. Please verify your account or contact support.");
        }

        Jwt accessTokenJwt = jwtService.generateAccessToken(employee);
        Jwt refreshTokenJwt = jwtService.generateRefreshToken(employee);

        String accessTokenString = accessTokenJwt.toString();
        String refreshTokenString = refreshTokenJwt.toString();

        Cookie cookie = new Cookie("refreshToken", refreshTokenString);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge(60 * 60 * 24 * 7);
        cookie.setSecure(cookieSecureFlag);
        response.addCookie(cookie);

        log.info("Employee {} (ID: {}) logged in successfully. Access token generated.", employee.getEmail(), employee.getId());
        return new LoginResponse(accessTokenString);
    }
}