package uz.umft.qabul.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "major_type")
public class MajorType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, columnDefinition = "integer default 1")
    private Integer active;

    public MajorType() {
    }

    public MajorType(String type, Integer active) {
        this.type = type;
        this.active = active;
    }
}
