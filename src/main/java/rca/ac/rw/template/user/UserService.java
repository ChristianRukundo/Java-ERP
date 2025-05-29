package rca.ac.rw.template.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rca.ac.rw.template.auth.dtos.RegisterRequestDto;
import rca.ac.rw.template.common.enums.EmployeeStatus;
import rca.ac.rw.template.common.enums.Role;
import rca.ac.rw.template.common.enums.Status;
import rca.ac.rw.template.common.exceptions.BadRequestException;
import rca.ac.rw.template.common.exceptions.ResourceNotFoundException;
import rca.ac.rw.template.common.exceptions.UnauthenticatedException;
import rca.ac.rw.template.common.exceptions.ValidationException;
import rca.ac.rw.template.employee.entity.Employee;
import rca.ac.rw.template.employee.repository.EmployeeRepository;
import rca.ac.rw.template.user.dtos.*;
import rca.ac.rw.template.user.converters.UserConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing user-related operations.
 * This includes user creation, password management, account activation, retrieval,
 * and admin operations for users.
 */
@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    public final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;
    @Value("${admin.password}")
    private String adminPassword;

     @Value("${admin.employee.code}") private String adminEmployeeCode; // Add this to properties
 @Value("${admin.phone.number:0795990111}") private String adminPhoneNumber;
 @Value("${admin.national.id:1200780136611025}") private String adminNationalId;

    public UserService(UserRepository userRepository, EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * Creates a new user in the system based on the registration request.
     * The new user is assigned ROLE_CUSTOMER, is initially disabled, and has PENDING status.
     *
     * @param registerRequestDto The DTO containing user registration details.
     * @return A {@link UserResponseDto} representing the newly created user.
     * @throws BadRequestException if a user with the provided email, phone number, or national ID already exists.
     */
//    @Transactional
//    public UserResponseDto createUser(RegisterRequestDto registerRequestDto) {
//        if (userRepository.existsByEmailOrPhoneNumberOrNationalId(registerRequestDto.email(), registerRequestDto.phoneNumber(), registerRequestDto.nationalId())) {
//            throw new BadRequestException("User with this email, national ID, or phone number already exists.");
//        }
//
//        User newUser = UserConverter.toEntity(registerRequestDto);
//        newUser.setPassword(passwordEncoder.encode(registerRequestDto.password()));
//        newUser.setRole(Role.ROLE_CUSTOMER);
//        newUser.setEnabled(false);
//        newUser.setStatus(Status.PENDING);
//
//        log.info("Attempting to create new user with email: {}", newUser.getEmail());
//        User savedUser = userRepository.save(newUser);
//        log.info("User created successfully with ID: {}", savedUser.getId());
//        log.info("User created successfully with created at : {}", savedUser.getCreatedAt());
//        return UserConverter.toUserResponseDto(savedUser);
//    }

    /**
     * Creates an administrative user if one does not already exist with the configured admin email.
     * This is typically called on application startup.
     */
    @Transactional
    public void createAdminUserIfNotExists() {
        // Check if an employee with this email already exists
        if (employeeRepository.findByEmail(adminEmail).isEmpty()) {
            Employee adminEmployee = new Employee();

            // User Part (inherited by Employee)
            adminEmployee.setFirstName("Default"); // Or from properties
            adminEmployee.setLastName("Admin");    // Or from properties
            adminEmployee.setEmail(adminEmail);
            adminEmployee.setPassword(passwordEncoder.encode(adminPassword));
            adminEmployee.setRole(Role.ROLE_ADMIN); // Set the Admin role
            adminEmployee.setStatus(EmployeeStatus.ACTIVE); // Admin should be active
            // adminEmployee.setEnabled(true); // This will be set by @PrePersist/@PreUpdate based on ACTIVE status
            adminEmployee.setMobile(adminPhoneNumber);
            adminEmployee.setNationalId(adminNationalId); // Use configured NID or default


            // Employee-Specific Part
            adminEmployee.setEmployeeCode(adminEmployeeCode); // Unique business code for the admin employee
            adminEmployee.setDateOfBirth(LocalDate.of(1970, 1, 1)); // Placeholder DOB for admin
            // Audit fields (createdAt, createdBy, etc.) will be handled by InitiatorAudit

            employeeRepository.save(adminEmployee);
            log.info("Admin Employee '{}' with code '{}' created.", adminEmail, adminEmployeeCode);
        } else {
            log.info("Admin Employee with email '{}' already exists.", adminEmail);
        }
    }
    /**
     * Finds a user by their email address. Publicly accessible.
     * @param email The email.
     * @return User entity.
     * @throws ResourceNotFoundException if user with email is not found.
     */
    @Transactional(readOnly = true) // Good practice for read methods
    public User findUserByActualEmail(String email) {
        log.debug("Finding user by actual email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @return UserProfileResponseDto containing the profile details.
     */
    @Transactional(readOnly = true)
    public UserProfileResponseDto getMyProfile() {
        User authenticatedUser = getAuthenticatedUser();
        return UserConverter.toUserProfileResponseDto(authenticatedUser);
    }

    /**
     * Updates the profile of the currently authenticated user.
     *
     * @param updateUserProfileRequestDto DTO containing the fields to update.
     * @return UserProfileResponseDto containing the updated profile details.
     */
//    @Transactional
//    public UserProfileResponseDto updateMyProfile(UpdateUserProfileRequestDto updateUserProfileRequestDto) {
//        User authenticatedUser = getAuthenticatedUser();
//
//        if (!authenticatedUser.getPhoneNumber().equals(updateUserProfileRequestDto.getPhoneNumber()) &&
//                userRepository.findByPhoneNumber(updateUserProfileRequestDto.getPhoneNumber()).isPresent()) {
//            throw new ValidationException("Phone number '" + updateUserProfileRequestDto.getPhoneNumber() + "' is already in use by another account.");
//        }
//
//        serConverter.updateUserFromDto(updateUserProfileRequestDto, authenticatedUser);
//
//        User updatedUser = userRepository.save(authenticatedUser);
//        log.info("User profile updated for user ID: {}", updatedUser.getId());
//        return UserConverter.toUserProfileResponseDto(updatedUser);
//    }

    /**
     * Retrieves the currently authenticated user entity.
     *
     * @return The {@link User} entity.
     * @throws UnauthenticatedException if no user is authenticated.
     * @throws ResourceNotFoundException if the authenticated user (by ID from token) is not found.
     */
    @Transactional(readOnly = true)
    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthenticatedException("User is not authenticated.");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UUID) {
            UUID userId = (UUID) principal;
            log.debug("Fetching authenticated user by ID: {}", userId); // Changed to debug
            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId.toString()));
        } else {
            log.error("Authenticated principal is not a UUID. Principal type: {}. Value: {}",
                    principal.getClass().getName(), principal.toString());
            throw new UnauthenticatedException("Unable to identify authenticated user from security principal.");
        }
    }

    /**
     * Finds a user by their email address. (Internal helper)
     * @param email The email.
     * @return User entity.
     */
    private User findUserByEmailInternal(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Finds a user by their ID. (Internal helper)
     * @param userId The user's ID.
     * @return User entity.
     */
    private User findUserByIdInternal(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
    }

    /**
     * Changes the password for a user identified by their email.
     *
     * @param userEmail The email of the user.
     * @param newPassword The new plain text password.
     */
    @Transactional
    public void changeUserPassword(String userEmail, String newPassword) {
        User user = findUserByEmailInternal(userEmail);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setStatus(Status.ACTIVE); // Password change often implies account becomes active
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", userEmail);
    }

    /**
     * Activates a user account (sets enabled to true and status to ACTIVE).
     *
     * @param userEmail The email of the user.
     */
    @Transactional
    public void activateUserAccount(String userEmail) {
        User user = findUserByEmailInternal(userEmail);
        user.setEnabled(true);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
        log.info("User account for email: {} activated.", userEmail);
    }

    /**
     * Updates the status of a user's account.
     *
     * @param email The email of the user.
     * @param newStatus The new status.
     */
    @Transactional
    public void updateUserStatus(String email, Status newStatus) {
        User user = findUserByEmailInternal(email);
        user.setStatus(newStatus);
        userRepository.save(user);
        log.info("User status for email: {} updated to {}.", email, newStatus);
    }


    // --- Admin Specific Methods ---

    /**
     * Retrieves a paginated list of all users for admin purposes.
     *
     * @param pageable Pagination and sorting information.
     * @param searchTerm Optional search term.
     * @param roleFilter Optional role to filter by.
     * @param statusFilter Optional status to filter by.
     * @param enabledFilter Optional enabled status to filter by.
     * @return A Page of UserProfileResponseDto.
     */
    @Transactional(readOnly = true)
    public Page<UserProfileResponseDto> getAllUsersForAdmin(Pageable pageable, String searchTerm, Role roleFilter, Status statusFilter, Boolean enabledFilter) {
        log.debug("Fetching all users for admin. Search: '{}', Role: {}, Status: {}, Enabled: {}", searchTerm, roleFilter, statusFilter, enabledFilter);
        Specification<User> spec = UserSpecifications.adminSearchUsers(searchTerm, roleFilter, statusFilter, enabledFilter);
        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserProfileResponseDto> dtos = userPage.getContent().stream()
                .map(UserConverter::toUserProfileResponseDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, userPage.getTotalElements());
    }



    /**
     * Retrieves a single user by ID for admin purposes.
     *
     * @param userId The ID of the user.
     * @return UserProfileResponseDto for the found user.
     */
    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserByIdForAdmin(UUID userId) {
        User user = findUserByIdInternal(userId);
        return UserConverter.toUserProfileResponseDto(user);
    }

    /**
     * Updates a user's details by an admin.
     *
     * @param userId The ID of the user to update.
     * @param updateDto DTO containing the fields to update.
     * @return UserProfileResponseDto of the updated user.
     */
    @Transactional
    public UserProfileResponseDto updateUserByAdmin(UUID userId, AdminUserUpdateRequestDto updateDto) {
        User user = findUserByIdInternal(userId);

        if (user.getEmail().equals(adminEmail) && updateDto.getRole() != null && updateDto.getRole() != Role.ROLE_ADMIN) {
            throw new ValidationException("Cannot change the role of the primary admin user (" + adminEmail + ") to a non-admin role.");
        }
        if (user.getEmail().equals(adminEmail) && updateDto.getEnabled() != null && !updateDto.getEnabled()) {
            throw new ValidationException("Cannot disable the primary admin user (" + adminEmail + ").");
        }
        if (updateDto.getPhoneNumber() != null && !user.getPhoneNumber().equals(updateDto.getPhoneNumber())) {
            userRepository.findByPhoneNumber(updateDto.getPhoneNumber())
                    .filter(u -> !u.getId().equals(userId)) // Check if it's a *different* user
                    .ifPresent(existingUser -> {
                        throw new ValidationException("Phone number '" + updateDto.getPhoneNumber() + "' is already in use by another user.");
                    });
        }

        UserConverter.updateUserFromAdminDto(updateDto, user);

        User savedUser = userRepository.save(user);
        log.info("User profile for ID: {} updated by admin.", savedUser.getId());
        return UserConverter.toUserProfileResponseDto(savedUser);
    }

    /**
     * Soft deletes a user by their ID, performed by an admin.
     *
     * @param userId The ID of the user to soft delete.
     */
    @Transactional
    public void softDeleteUserByAdmin(UUID userId) {
        User user = findUserByIdInternal(userId);
        if (user.getEmail().equals(adminEmail)) {
            throw new ValidationException("The primary admin user (" + adminEmail + ") cannot be deleted.");
        }
        // The @SQLDelete annotation on User entity handles setting 'deleted = true'
        userRepository.delete(user);
        log.info("User with ID: {} soft-deleted by admin.", userId);
    }

    /**
     * Activates a user account by admin (sets enabled and status to ACTIVE).
     * @param userId ID of the user to activate.
     */
    @Transactional
    public void activateUserAccountByAdmin(UUID userId) {
        User user = findUserByIdInternal(userId);
        if (user.isEnabled() && user.getStatus() == Status.ACTIVE) {
            log.info("User {} is already active and enabled.", userId);
            return;
        }
        user.setEnabled(true);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
        log.info("User account for ID: {} activated by admin.", userId);
    }

    /**
     * Updates the status of a user account by admin.
     * @param userId ID of the user.
     * @param newStatus The new Status.
     */
    @Transactional
    public void updateUserStatusByAdmin(UUID userId, Status newStatus) {
        User user = findUserByIdInternal(userId);
        if (user.getEmail().equals(adminEmail) && (newStatus != Status.ACTIVE)) {

            if (newStatus == Status.PENDING || newStatus == Status.RESET) {
                throw new ValidationException("The primary admin user (" + adminEmail + ") status cannot be set to PENDING or RESET.");
            }
        }
        user.setStatus(newStatus);
        userRepository.save(user);
        log.info("User status for ID: {} updated to {} by admin.", userId, newStatus);
    }
}