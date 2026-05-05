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

    @Column(unique = true, nullable = false)
    private Long phoneNumber;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role type;

    // default UZ
    @Enumerated(EnumType.STRING)
    private Lang lang;

    @CreationTimestamp(source = SourceType.VM)
    private LocalDateTime createdAt;

    @UpdateTimestamp(source = SourceType.VM)
    private LocalDateTime updatedAt;
}

