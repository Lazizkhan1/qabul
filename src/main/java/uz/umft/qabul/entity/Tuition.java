package uz.umft.qabul.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import uz.umft.qabul.enums.Degree;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "tuition")
public class Tuition {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "major_code")
    private Integer majorCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_year")
    private SchoolYear schoolYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "major_type_id", nullable = false)
    private MajorType majorType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "major_lang_id", nullable = false)
    private MajorLang majorLang;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Degree degree;

    @Column(nullable = false)
    private Long amount;
}
