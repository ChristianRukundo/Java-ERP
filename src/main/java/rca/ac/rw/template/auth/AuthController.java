package rca.ac.rw.template.auth;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import rca.ac.rw.template.auth.dtos.*;
import rca.ac.rw.template.common.exceptions.BadRequestException;
import rca.ac.rw.template.email.EmailService;
import rca.ac.rw.template.employee.dto.EmployeeProfileResponseDto;
import rca.ac.rw.template.employee.entity.Employee;
import rca.ac.rw.template.employee.service.EmployeeService;

/**
 * Controller for handling authentication processes such as login, employee self-registration,
 * account verification, and password reset for the ERP system.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final EmployeeService employeeService;
    private final OtpService otpService;
    private final EmailService emailService;

    /**
     * Handles employee self-registration.
     * Creates a new Employee with default roles/status and sends a verification OTP.
     *
     * @param registerDto DTO containing basic employee registration details.
     * @param uriBuilder  For building the URI of the created resource.
     * @return ResponseEntity with {@link EmployeeProfileResponseDto} and HTTP 201 Created status.
     */
    @PostMapping("/register")
    @RateLimiter(name = "auth-rate-limiter")
    public ResponseEntity<EmployeeProfileResponseDto> registerEmployee(
            @Valid @RequestBody RegisterRequestDto registerDto,
            UriComponentsBuilder uriBuilder) {
        log.info("Employee self-registration attempt for email: {}", registerDto.email());

        EmployeeProfileResponseDto employeeResponse = employeeService.selfRegisterEmployee(registerDto);

        var uri = uriBuilder.path("/api/v1/employee/profile/me")
                .buildAndExpand(employeeResponse.getId()).toUri();

        log.info("Employee {} registered successfully via self-service with ID {}. Sending verification OTP.",
                employeeResponse.getEmail(), employeeResponse.getId());

        String otp = otpService.generateAndStoreOtp(employeeResponse.getEmail(), OtpType.VERIFY_ACCOUNT);
        emailService.sendAccountVerificationEmail(
                employeeResponse.getEmail(),
                employeeResponse.getFirstName() + " " + employeeResponse.getLastName(),
                otp
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(employeeResponse);
    }

    /**
     * Handles employee login using email and password.
     *
     * @param loginRequestDto DTO containing login credentials (expects 'email').
     * @param response        HttpServletResponse to add the refresh token cookie.
     * @return ResponseEntity with {@link LoginResponse} containing the access token.
     */
    @PostMapping("/login")
    @RateLimiter(name = "auth-rate-limiter")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequestDto loginRequestDto,
            HttpServletResponse response) {
        log.info("Login attempt for email: {}", loginRequestDto.email());
        LoginResponse loginResult = authService.login(loginRequestDto, response);
        return ResponseEntity.ok(loginResult);
    }

    /**
     * Verifies an employee's account using an OTP.
     *
     * @param verifyAccountRequest DTO containing email and OTP.
     * @return ResponseEntity indicating success or failure.
     */
    @PatchMapping("/verify-account")
    @RateLimiter(name = "otp-rate-limiter")
    public ResponseEntity<String> verifyAccount(@Valid @RequestBody VerifyAccountDto verifyAccountRequest) {
        log.info("Attempting to verify account for email: {}", verifyAccountRequest.email());
        if (!otpService.verifyOtp(verifyAccountRequest.email(), verifyAccountRequest.otp(), OtpType.VERIFY_ACCOUNT)) {
            throw new BadRequestException("Invalid or expired OTP provided for account verification.");
        }

        Employee employee = employeeService.activateEmployeeAccount(verifyAccountRequest.email());
        log.info("Account for email {} successfully verified and activated.", verifyAccountRequest.email());

        emailService.sendVerificationSuccessEmail(employee.getEmail(), employee.getFirstName() + " " + employee.getLastName());
        return ResponseEntity.ok("Account activated successfully. You can now log in.");
    }

    /**
     * Initiates the password reset process for an employee.
     *
     * @param initiateRequest DTO containing the employee's email.
     * @return ResponseEntity with a confirmation message.
     */
    @PostMapping("/initiate-password-reset")
    @RateLimiter(name = "auth-rate-limiter")
    public ResponseEntity<String> initiatePasswordReset(@Valid @RequestBody InitiatePasswordResetDto initiateRequest) {
        log.info("Password reset initiated for email: {}", initiateRequest.email());
        Employee employee = employeeService.findEmployeeByEmailForPasswordReset(initiateRequest.email());

        String otp = otpService.generateAndStoreOtp(employee.getEmail(), OtpType.FORGOT_PASSWORD);
        employeeService.setEmployeeStatusToReset(employee.getEmail());

        emailService.sendResetPasswordOtp(employee.getEmail(), employee.getFirstName() + " " + employee.getLastName(), otp);
        return ResponseEntity.ok("If your email is registered, a password reset OTP has been sent.");
    }

    /**
     * Resets an employee's password using a valid OTP.
     *
     * @param resetPasswordRequest DTO containing email, OTP, and new password.
     * @return ResponseEntity indicating success.
     */
    @PatchMapping("/reset-password")
    @RateLimiter(name = "auth-rate-limiter")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordRequest) {
        log.info("Attempting to reset password for email: {}", resetPasswordRequest.email());
        if (!otpService.verifyOtp(resetPasswordRequest.email(), resetPasswordRequest.otp(), OtpType.FORGOT_PASSWORD)) {
            throw new BadRequestException("Invalid or expired OTP provided for password reset.");
        }

        employeeService.resetEmployeePassword(resetPasswordRequest.email(), resetPasswordRequest.newPassword());
        Employee employee = employeeService.findEmployeeByEmailForPasswordReset(resetPasswordRequest.email());

        emailService.sendResetPasswordSuccessEmail(employee.getEmail(), employee.getFirstName() + " " + employee.getLastName());
        return ResponseEntity.ok("Password has been reset successfully. You can now log in with your new password.");
    }
}