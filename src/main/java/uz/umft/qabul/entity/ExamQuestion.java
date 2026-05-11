package uz.umft.qabul.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "exam_questions")
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "question_text", columnDefinition = "text")
    private String questionText;

    private Double point;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_lang_id")
    private MajorLang majorLang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;
}
