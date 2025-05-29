package rca.ac.rw.template.auth.dtos;

import jakarta.validation.constraints.*; // For NotBlank, Email, Size, Past, NotNull
import rca.ac.rw.template.common.validation.ValidPassword; // Assuming you have this
import rca.ac.rw.template.common.validation.ValidRwandaId;
import rca.ac.rw.template.common.validation.ValidRwandanPhoneNumber;
// import rca.ac.rw.template.common.validation.ValidRwandanPhoneNumber; // If using specific Rwandan validation
// import rca.ac.rw.template.common.validation.ValidRwandaId; // If using specific Rwandan validation

import java.time.LocalDate;

public record RegisterRequestDto(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50)
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Phone number (mobile) is required.")
        @ValidRwandanPhoneNumber
        String phoneNumber, // Changed from mobile to be consistent

        @NotBlank(message = "National ID is required.")
        @ValidRwandaId // Use if available
        String nationalId,

        @NotNull(message = "Date of birth is required") // ADDED
        @Past(message = "Date of birth must be in the past")    // ADDED
        LocalDate dateOfBirth,                                 // ADDED (matches Employee.dateOfBirth)

        @NotBlank(message = "Password is required")
        @ValidPassword
        String password
) {
}