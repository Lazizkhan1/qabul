package uz.umft.qabul.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import uz.umft.qabul.domain.enums.Lang;
import uz.umft.qabul.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Lang lang;

    @CreationTimestamp(source = SourceType.VM)
    private LocalDateTime createdAt;

    @UpdateTimestamp(source = SourceType.VM)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (lang == null) {
            lang = Lang.UZ;
        }
    }
}
