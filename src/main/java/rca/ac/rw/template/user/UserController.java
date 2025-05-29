package rca.ac.rw.template.user; // Or rca.ac.rw.template.profile

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rca.ac.rw.template.user.dtos.UpdateUserProfileRequestDto;
import rca.ac.rw.template.user.dtos.UserProfileResponseDto;


@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService userService;




}