package uz.umft.qabul.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.config.AuthProperties;
import uz.umft.qabul.domain.entity.User;
import uz.umft.qabul.domain.enums.Lang;
import uz.umft.qabul.domain.enums.Role;
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
        if (authProperties.adminUsername().isBlank() || authProperties.adminPassword().isBlank()) {
            throw AuthException.badRequest("MISSING_ADMIN_CONFIG", "Admin credentials are not configured");
        }

        User admin = userRepository.findByUsername(authProperties.adminUsername())
                .orElseGet(User::new);
        admin.setUsername(authProperties.adminUsername());
        admin.setPhoneNumber(null);
        admin.setPasswordHash(passwordEncoder.encode(authProperties.adminPassword()));
        admin.setType(Role.ADMIN);
        admin.setLang(Lang.UZ);
        userRepository.save(admin);
        log.info("Admin account upserted for username {}", authProperties.adminUsername());
    }
}
