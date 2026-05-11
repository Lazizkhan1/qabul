package uz.umft.qabul.config;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import uz.umft.qabul.entity.*;
import uz.umft.qabul.enums.CertType;
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
    private final SchoolYearRepository schoolYearRepository;
    private final TuitionRepository tuitionRepository;
    private final CertCategoryRepository certCategoryRepository;
    private final SubjectRepository subjectRepository;

    private record MajorData(Integer code, String title, String type, Long amount, String sub2, String sub3) {
    }

    private static final List<MajorData> INITIAL_MAJORS = List.of(
            new MajorData(60110100, "Pedagogika", "Kunduzgi", 18_000_000L, "Biologiya", "Ona tili va adabiyoti"),
            new MajorData(60110900, "Xorijiy til va adabiyoti", "Kunduzgi", 18_000_000L, "Xorijiy til", "Ona tili va adabiyoti"),
            new MajorData(60310300, "Psixologiya", "Kunduzgi", 16_000_000L, "Biologiya", "Ona tili va adabiyoti"),
            new MajorData(60410100, "Iqtisodiyot", "Kunduzgi", 19_200_000L, "Matematika", "Xorijiy til"),
            new MajorData(60410500, "Moliya va moliyaviy texnologiyalar", "Kunduzgi", 19_200_000L, "Matematika", "Xorijiy til"),
            new MajorData(60410800, "Menejment", "Kunduzgi", 18_000_000L, "Matematika", "Xorijiy til"),
            new MajorData(61010400, "Logistika", "Kunduzgi", 18_000_000L, "Matematika", "Xorijiy til"),
            new MajorData(60610200, "Axborot xavfsizligi", "Kunduzgi", 18_000_000L, "Matematika", "Fizika"),
            new MajorData(60610300, "Kompyuter injiniringi", "Kunduzgi", 18_000_000L, "Matematika", "Fizika"),
            new MajorData(60610400, "Dasturiy injiniring", "Kunduzgi", 18_000_000L, "Matematika", "Fizika"),
            new MajorData(60610500, "Sun'iy intellekt (Janubiy Koreya)", "Kunduzgi", 32_000_000L, "Matematika", "Fizika"),
            new MajorData(60610501, "Sun'iy intellekt", "Kunduzgi", 18_000_000L, "Matematika", "Fizika"),
            new MajorData(60611100, "Infokommunikatsiya injiniringi", "Kunduzgi", 18_000_000L, "Matematika", "Fizika"),
            new MajorData(70410110, "Raqamli iqtisodiyot", "Kunduzgi", 18_000_000L, "Matematika", "Xorijiy til"),
            new MajorData(70610101, "Kompyuter tizimlari va ularning dasturiy ta'minoti", "Kunduzgi", 18_000_000L, "Matematika", "Fizika"),
            new MajorData(70610601, "Telekomunikatsiya injiniringi", "Kunduzgi", 18_000_000L, "Matematika", "Fizika")
    );

    @Override
    public void run(@NonNull ApplicationArguments args) {

        log.info("Initializing database");
        SchoolYear schoolYear;
        if (schoolYearRepository.count() == 0) {
            schoolYear = new SchoolYear();
            schoolYear.setTitle("2026-2027");
            schoolYear.setActive(true);
            schoolYearRepository.save(schoolYear);
        } else {
            schoolYear = schoolYearRepository.findAll().stream()
                    .filter(sy -> Boolean.TRUE.equals(sy.getActive()))
                    .findFirst()
                    .orElseGet(() -> {
                        SchoolYear created = new SchoolYear();
                        created.setTitle("2026-2027");
                        created.setActive(true);
                        return schoolYearRepository.save(created);
                    });
        }

        MajorType kunduzgi;
        if (majorTypeRepository.count() == 0) {
            kunduzgi = majorTypeRepository.save(
                    new MajorType("Kunduzgi", 1)
            );
        } else {
            kunduzgi = majorTypeRepository.findByType("Kunduzgi")
                    .orElseGet(() -> majorTypeRepository.save(new MajorType("Kunduzgi", 1)));
        }
        MajorLang uzbek;
        MajorLang rus;

        if (majorLangRepository.count() == 0) {
            uzbek = majorLangRepository.save(new MajorLang("O'zbek"));
            rus = majorLangRepository.save(new MajorLang("Rus"));
        } else {
            uzbek = majorLangRepository.findByLang("O'zbek")
                    .orElseGet(() -> majorLangRepository.save(new MajorLang("O'zbek")));
            rus = majorLangRepository.findByLang("Rus")
                    .orElseGet(() -> majorLangRepository.save(new MajorLang("Rus")));
        }

        Subject s1 = subjectRepository.findByTitle("Ona tili va adabiyoti")
                .orElseGet(() -> {
                    Subject s = new Subject();
                    s.setTitle("Ona tili va adabiyoti");
                    s.setTotalQuestions(30);
                    return subjectRepository.save(s);
                });
        Subject sMat = subjectRepository.findByTitle("Matematika")
                .orElseGet(() -> {
                    Subject s = new Subject();
                    s.setTitle("Matematika");
                    s.setTotalQuestions(30);
                    return subjectRepository.save(s);
                });
        Subject sFiz = subjectRepository.findByTitle("Fizika")
                .orElseGet(() -> {
                    Subject s = new Subject();
                    s.setTitle("Fizika");
                    s.setTotalQuestions(30);
                    return subjectRepository.save(s);
                });
        Subject sBio = subjectRepository.findByTitle("Biologiya")
                .orElseGet(() -> {
                    Subject s = new Subject();
                    s.setTitle("Biologiya");
                    s.setTotalQuestions(30);
                    return subjectRepository.save(s);
                });
        Subject sXor = subjectRepository.findByTitle("Xorijiy til")
                .orElseGet(() -> {
                    Subject s = new Subject();
                    s.setTitle("Xorijiy til");
                    s.setTotalQuestions(30);
                    return subjectRepository.save(s);
                });

        if (majorRepository.count() == 0) {
            for (MajorData item : INITIAL_MAJORS) {
                Major major = new Major();
                major.setTitle(item.title());
                major.setSubject1(s1);
                major.setSubject2(subjectRepository.findByTitle(item.sub2()).orElseThrow());
                major.setSubject3(subjectRepository.findByTitle(item.sub3()).orElseThrow());
                majorRepository.save(major);
            }
        }

        if (tuitionRepository.count() == 0) {
            for (MajorLang lang : List.of(uzbek, rus)) {
                for (MajorData item : INITIAL_MAJORS) {
                    Tuition tuition = new Tuition();
                    tuition.setMajorCode(item.code());
                    tuition.setSchoolYear(schoolYear);
                    tuition.setMajor(majorRepository.findByTitle(item.title()).orElseThrow());
                    tuition.setMajorType(kunduzgi);
                    tuition.setAmount(item.amount());
                    tuition.setDegree(Degree.BACHELOR);
                    tuition.setMajorLang(lang);
                    tuitionRepository.save(tuition);
                }
            }
        }


        if (certCategoryRepository.count() == 0) {
            certCategoryRepository.saveAll(List.of(
                    new CertCategory("IELTS", CertType.LANGUAGE),
                    new CertCategory("TOEFL", CertType.LANGUAGE),
                    new CertCategory("CEFR", CertType.LANGUAGE),

                    new CertCategory("Matematika", CertType.NATIONAL),
                    new CertCategory("Fizika", CertType.NATIONAL),
                    new CertCategory("Biologiya", CertType.NATIONAL),
                    new CertCategory("Kimyo", CertType.NATIONAL),
                    new CertCategory("Tarix", CertType.NATIONAL),
                    new CertCategory("Geografiya", CertType.NATIONAL),
                    new CertCategory("Huquqshunoslik", CertType.NATIONAL),
                    new CertCategory("Ona tili va adabiyoti", CertType.NATIONAL),
                    new CertCategory("Rus tili va adabiyoti", CertType.NATIONAL),
                    new CertCategory("Qoraqalpoq tili va adabiyoti", CertType.NATIONAL),

                    new CertCategory("DTM", CertType.DTM)
            ));
        }
    }
}
