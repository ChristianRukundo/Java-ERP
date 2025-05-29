package rca.ac.rw.template.employee.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSelfProfileRequestDto {
    @Size(min = 2, max = 50)
    private String firstName;

    @Size(min = 2, max = 50)
    private String lastName;

    @Pattern(regexp = "^(072|073|078|079)\\d{7}$", message = "Invalid Rwandan mobile number format.")
    private String mobile; // or phoneNumber
    // Address if applicable
}