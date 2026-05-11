package uz.umft.qabul.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.controller.dto.user.CreateModeratorRequest;
import uz.umft.qabul.controller.dto.user.UserResponse;
import uz.umft.qabul.dto.PagedResponse;
import uz.umft.qabul.enums.Role;
import uz.umft.qabul.service.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/applicants")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public PagedResponse<UserResponse> listApplicants(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        return userService.listByRole(Role.APPLICANT, page, limit);
    }

    @PostMapping("/moderators")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createModerator(@Valid @RequestBody CreateModeratorRequest request) {
        return userService.createModerator(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}
