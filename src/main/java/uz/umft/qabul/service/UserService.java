package uz.umft.qabul.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.controller.dto.user.CreateModeratorRequest;
import uz.umft.qabul.controller.dto.user.UserResponse;
import uz.umft.qabul.dto.PagedResponse;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.Role;
import uz.umft.qabul.exception.AuthException;
import uz.umft.qabul.repository.UserRepository;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PagedResponse<UserResponse> listByRole(Role role, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<User> userPage = userRepository.findByType(role, pageable);
        return new PagedResponse<>(
                page,
                limit,
                userPage.getTotalElements(),
                userPage.getContent().stream().map(this::mapToResponse).toList()
        );
    }

    @Transactional
    public UserResponse createModerator(CreateModeratorRequest request) {
        if (userRepository.existsByPhoneNumber(request.username())) {
            throw AuthException.conflict("USER_EXISTS", "Username already exists");
        }
        User user = new User();
        user.setPhoneNumber(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setType(Role.MODERATOR);
        user.setLang(request.lang());
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }

    public UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getPhoneNumber(),
                user.getType(),
                user.getLang(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
