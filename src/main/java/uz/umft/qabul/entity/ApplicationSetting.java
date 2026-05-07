package uz.umft.qabul.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "application_settings")
public class ApplicationSetting {

    @Id
    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "value_boolean", nullable = false)
    private boolean valueBoolean;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;
}
