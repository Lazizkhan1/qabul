package uz.umft.qabul.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "major")
public class Major {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject1_id")
    private Subject subject1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject2_id")
    private Subject subject2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject3_id")
    private Subject subject3;

    public Major() {
    }

    public Major(String title) {
        this.title = title;
    }

}
