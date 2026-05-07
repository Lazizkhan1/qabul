package uz.umft.qabul.config;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import uz.umft.qabul.entity.*;
import uz.umft.qabul.enums.Degree;
import uz.umft.qabul.repository.*;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AppInit implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AppInit.class);
    private final MajorRepository majorRepository;
    private final MajorTypeRepository majorTypeRepository;
    private final MajorLangRepository majorLangRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final TuitionRepository tuitionRepository;

    private record MajorData(Integer code, String title, String type) {}

    private static final List<MajorData> INITIAL_MAJORS = List.of(
            new MajorData(60110100, "Pedagogika", "Kunduzgi"),
            new MajorData(60110900, "Xorijiy til va adabiyoti", "Kunduzgi"),
            new MajorData(60310300, "Psixologiya", "Kunduzgi"),
            new MajorData(60410100, "Iqtisodiyot", "Kunduzgi"),
            new MajorData(60410500, "Moliya va moliyaviy texnologiyalar", "Kunduzgi"),
            new MajorData(60410800, "Menejment", "Kunduzgi"),
            new MajorData(61010400, "Logistika", "Kunduzgi"),
            new MajorData(60610200, "Axborot xavfsizligi", "Kunduzgi"),
            new MajorData(60610300, "Kompyuter injiniringi", "Kunduzgi"),
            new MajorData(60610400, "Dasturiy injiniring", "Kunduzgi"),
            new MajorData(60611100, "Infokommunikatsiya injiniringi", "Kunduzgi"),
            new MajorData(60610500, "Sun'iy intellekt", "Kunduzgi"),
            new MajorData(70410110, "Raqamli iqtisodiyot", "Kunduzgi"),
            new MajorData(70610101, "Kompyuter tizimlari va ularning dasturiy ta'minoti", "Kunduzgi"),
            new MajorData(70610601, "Telekomunikatsiya injiniringi", "Kunduzgi")
    );

    @Override
    public void run(@NonNull ApplicationArguments args) {
        log.error("SHEEESH");
        if (majorRepository.count() > 0) {
            return;
        }
        log.info("Initializing database");

        // Create defaults
        MajorLang defaultLang = majorLangRepository.findByLang("O'zbek")
                .orElseGet(() -> {
                    MajorLang lang = new MajorLang();
                    lang.setLang("O'zbek");
                    return majorLangRepository.save(lang);
                });

        Subject defaultSubject = subjectRepository.findByTitle("Matematika")
                .orElseGet(() -> {
                    Subject subject = new Subject();
                    subject.setTitle("Matematika");
                    subject.setExamDuration(180);
                    subject.setTotalQuestions(30);
                    return subjectRepository.save(subject);
                });

        SchoolYear defaultYear = schoolYearRepository.findByTitle("2026-2027")
                .orElseGet(() -> {
                    SchoolYear year = new SchoolYear();
                    year.setTitle("2026-2027");
                    year.setActive(true);
                    return schoolYearRepository.save(year);
                });

        for (MajorData item : INITIAL_MAJORS) {
            Major major = majorRepository.findByTitle(item.title())
                    .orElseGet(() -> {
                        Major m = new Major();
                        m.setTitle(item.title());
                        return majorRepository.save(m);
                    });

            MajorType majorType = majorTypeRepository.findByType(item.type())
                    .orElseGet(() -> {
                        MajorType mt = new MajorType();
                        mt.setType(item.type());
                        return majorTypeRepository.save(mt);
                    });

            Tuition tuition = new Tuition();
            tuition.setMajorCode(item.code());
            tuition.setMajor(major);
            tuition.setMajorType(majorType);
            tuition.setMajorLang(defaultLang);
            tuition.setSubject(defaultSubject);
            tuition.setSchoolYear(defaultYear);
            tuition.setDegree(Degree.BACHELOR);
            tuition.setAmount(18_000_000.0);
            tuitionRepository.save(tuition);
        }
    }
}
