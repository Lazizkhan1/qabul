package uz.umft.qabul.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.config.AuthProperties;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.Lang;
import uz.umft.qabul.enums.Role;
import uz.umft.qabul.exception.AuthException;
import uz.umft.qabul.repository.UserRepository;

@Service
public class AdminBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;

    public AdminBootstrapService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthProperties authProperties
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void upsertAdmin() {
        if (authProperties.adminPhone().isBlank() || authProperties.adminPassword().isBlank()) {
            throw AuthException.badRequest("MISSING_ADMIN_CONFIG", "Admin credentials are not configured");
        }

        User admin = userRepository.findByPhoneNumber(authProperties.adminPhone())
                .orElseGet(User::new);
        admin.setPhoneNumber(authProperties.adminPhone());
        admin.setPasswordHash(passwordEncoder.encode(authProperties.adminPassword()));
        admin.setType(Role.ADMIN);
        admin.setLang(Lang.UZ);
        userRepository.save(admin);
        log.info("Admin account upserted for phone {}", authProperties.adminPhone());
    }
}
