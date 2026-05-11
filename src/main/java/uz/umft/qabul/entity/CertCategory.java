package uz.umft.qabul.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.umft.qabul.enums.CertType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cert_category")
public class CertCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CertType type;

    public CertCategory(String title, CertType type) {
        this.title = title;
        this.type = type;
    }
}
