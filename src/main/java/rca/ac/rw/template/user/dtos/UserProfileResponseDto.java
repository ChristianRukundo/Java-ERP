package rca.ac.rw.template.user.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rca.ac.rw.template.user.Address;
import rca.ac.rw.template.common.enums.Role;
import rca.ac.rw.template.common.enums.Status;


import java.time.LocalDateTime; // For created/updated timestamps if you expose them
import java.util.UUID;

/**
 * Data Transfer Object for responding with detailed user profile information.
 */
@Data @NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email; // Usually non-editable by user directly
    private String phoneNumber;
    private String nationalId; // Usually non-editable by user directly
    private Status status;
    private Address address;
    private Role role;
    private boolean enabled;

    // Audit fields (optional, if you want to show them on a profile)
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

}