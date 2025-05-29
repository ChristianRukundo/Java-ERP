package rca.ac.rw.template.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor; // Or @RequiredArgsConstructor if fields are final
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; // For configurable 'from' email
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime; // Import LocalDateTime

@Service
@AllArgsConstructor // Ensure JavaMailSender and ITemplateEngine are final if using this
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;

    @Value("${spring.mail.username}") // For setting a dynamic 'from' address if it's the same as username
    private String mailFromAddress;

    @Value("${app.company.name:National Bank of Rwanda}") // Default company name
    private String appCompanyName;

    @Value("${app.email.logo.url:https://via.placeholder.com/150x50.png?text=Bank+Logo}") // Default logo
    private String appLogoUrl;


    // --- OTP and Account Management Emails ---
    @Async
    public void sendAccountVerificationEmail(String to, String customerFullName, String otp) {
        final String TEMPLATE_NAME = "verify_account"; // Ensure this template exists
        final String SUBJECT = "Verify Your " + appCompanyName + " Account - OTP";
        log.debug("Preparing to send '{}' email to '{}'", SUBJECT, to);
        try {
            Context context = new Context();
            context.setVariable("name", customerFullName);
            context.setVariable("otp", otp);
            context.setVariable("companyName", appCompanyName);
            context.setVariable("expirationTimeMinutes", "10"); // Standard OTP expiry
            context.setVariable("companyLogoUrl", appLogoUrl);
            // context.setVariable("verificationUrl", "YOUR_FRONTEND_VERIFICATION_URL"); // If applicable

            sendEmailWithTemplate(TEMPLATE_NAME, context, to, SUBJECT);
        } catch (Exception e) {
            log.error("Error preparing or sending account verification email to {}: {}", to, e.getMessage(), e);
        }
    }


    @Async
    public void sendResetPasswordOtp(String to, String customerFullName, String otp) {
        final String TEMPLATE_NAME = "forgot_password"; // Ensure this template exists
        final String SUBJECT = "Reset Your " + appCompanyName + " Password - OTP";
        log.debug("Preparing to send '{}' email to '{}'", SUBJECT, to);
        try {
            Context context = new Context();
            context.setVariable("name", customerFullName);
            context.setVariable("otp", otp);
            context.setVariable("companyName", appCompanyName);
            context.setVariable("expirationTimeMinutes", "10"); // Consider longer for password reset
            context.setVariable("companyLogoUrl", appLogoUrl);
            // context.setVariable("passwordResetUrl", "YOUR_FRONTEND_PASSWORD_RESET_URL"); // If applicable

            sendEmailWithTemplate(TEMPLATE_NAME, context, to, SUBJECT);
        } catch (Exception e) {
            log.error("Error preparing or sending password reset OTP email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async
    public void sendVerificationSuccessEmail(String to, String customerFullName) {
        sendGeneralSuccessEmail(to, customerFullName, "verify_success", "Your Account Has Been Verified!");
    }

    @Async
    public void sendResetPasswordSuccessEmail(String to, String customerFullName) {
        sendGeneralSuccessEmail(to, customerFullName, "reset_success", "Your Password Has Been Reset Successfully");
    }


    /**
     * Sends an email notification to the employee when their salary is credited (payroll approved).
     * Format: Dear <FIRSTNAME>, your salary for <MONTH>/<YEAR> from <INSTITUTION>
     * amounting to <AMOUNT> has been credited to your account <EMPLOYEE ID> successfully.
     *
     * @param toEmail          Employee's email.
     * @param employeeFirstName Employee's first name.
     * @param monthYear        The payroll period (e.g., "12/2025").
     * @param institutionName  The name of the institution/employer.
     * @param netSalaryAmount  The net salary credited.
     * @param employeeIdentifier The employee's code or ID.
     */
    @Async
    public void sendSalaryCreditedEmail(String toEmail, String employeeFirstName,
                                        String monthYear, String institutionName,
                                        BigDecimal netSalaryAmount, String employeeIdentifier) {
        final String TEMPLATE_NAME = "salary_credited_notification"; // Create this Thymeleaf template
        final String SUBJECT = "Salary Credited for " + monthYear;
        log.debug("Preparing salary credited notification for {} for period {}", employeeFirstName, monthYear);
        try {
            Context context = new Context();
            context.setVariable("firstName", employeeFirstName);
            context.setVariable("monthYear", monthYear);
            context.setVariable("institutionName", institutionName);
            context.setVariable("netAmount", netSalaryAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
            context.setVariable("employeeIdentifier", employeeIdentifier); // This is EMPLOYEE ID/CODE
            context.setVariable("companyName", appCompanyName); // General company name for footer
            context.setVariable("companyLogoUrl", appLogoUrl);

            sendEmailWithTemplate(TEMPLATE_NAME, context, toEmail, SUBJECT);
        } catch (Exception e) {
            log.error("Error preparing or sending salary credited email to {}: {}", toEmail, e.getMessage(), e);
        }
    }

    // --- Private Helper Methods ---
    private void sendGeneralSuccessEmail(String to, String customerFullName, String templateName, String subject) {
        log.debug("Preparing general success email '{}' to '{}'", subject, to);
        try {
            Context context = new Context();
            context.setVariable("name", customerFullName); // Consistent with OTP emails
            context.setVariable("companyName", appCompanyName);
            context.setVariable("companyLogoUrl", appLogoUrl);
            sendEmailWithTemplate(templateName, context, to, subject);
        } catch (Exception e) {
            log.error("Error preparing or sending general success email '{}' to {}: {}", subject, to, e.getMessage(), e);
        }
    }


    private void sendEmailWithTemplate(String templateName, Context context, String to, String subject) {
        try {
            String htmlContent = templateEngine.process(templateName, context);
            sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            log.error("Failed to process template '{}' for email [{}] to {}. Error: {}", templateName, subject, to, e.getMessage(), e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (to == null || to.trim().isEmpty()) {
            log.warn("Attempted to send email with subject '{}' but 'to' address was null or empty. Skipping.", subject);
            return;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(mailFromAddress); // Use configured 'from' address
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("Successfully sent email '{}' to {}", subject, to);
        } catch (MessagingException e) {
            // This will catch the original SocketException if it's a mail sending issue
            log.error("MessagingException sending email '{}' to {}: {}", subject, to, e.getMessage(), e);
            // Consider re-throwing a custom application exception or returning a status
            // if email sending failure is critical and needs to be handled by the caller.
            // For now, just logging.
        } catch (Exception e) {
            log.error("Unexpected error sending email '{}' to {}: {}", subject, to, e.getMessage(), e);
        }
    }
}