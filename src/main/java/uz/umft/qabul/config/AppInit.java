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
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamAnswerRepository examAnswerRepository;

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

        Subject sTarix = subjectRepository.findByTitle("Tarix")
                .orElseGet(() -> {
                    Subject s = new Subject();
                    s.setTitle("Tarix");
                    s.setTotalQuestions(30);
                    return subjectRepository.save(s);
                });

        Subject sChet = subjectRepository.findByTitle("Chet tili")
                .orElseGet(() -> {
                    Subject s = new Subject();
                    s.setTitle("Chet tili");
                    s.setTotalQuestions(30);
                    return subjectRepository.save(s);
                });

        Subject sMatMajburiy = subjectRepository.findByTitle("Matematika (majburiy)")
                .orElseGet(() -> {
                    Subject s = new Subject();
                    s.setTitle("Matematika (majburiy)");
                    s.setTotalQuestions(10);
                    return subjectRepository.save(s);
                });

        Subject sOnaMajburiy = subjectRepository.findByTitle("Ona tili (majburiy)")
                .orElseGet(() -> {
                    Subject s = new Subject();
                    s.setTitle("Ona tili (majburiy)");
                    s.setTotalQuestions(10);
                    return subjectRepository.save(s);
                });

        Subject sUzTarix = subjectRepository.findByTitle("O'zbekiston tarixi")
                .orElseGet(() -> {
                    Subject s = new Subject();
                    s.setTitle("O'zbekiston tarixi");
                    s.setTotalQuestions(10);
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

        seedRealQuestions(sTarix, 3.1, uzbek, List.of(
                new QuestionSeed("Birinchi jahon urushi yillari?", "1914-1918", "1939-1945", "1917-1921", "1905-1911"),
                new QuestionSeed("Napoleon Bonapart qayerda vafot etgan?", "Avliyo Yelena oroli", "Elba oroli", "Parij", "Moskva"),
                new QuestionSeed("Misr piramidalari poytaxti?", "Giza", "Qohira", "Aleksandriya", "Luksor"),
                new QuestionSeed("Rim imperiyasi qachon parchalangan?", "395-yil", "476-yil", "313-yil", "1453-yil"),
                new QuestionSeed("Amerika qachon kashf etilgan?", "1492", "1498", "1502", "1519"),
                new QuestionSeed("Renessans vatani?", "Italiya", "Fransiya", "Gretsiya", "Germaniya"),
                new QuestionSeed("Buyuk ipak yo'li asoschisi (asosan)?", "Chjan Syan", "Marko Polo", "Ibn Battuta", "Vasko da Gama"),
                new QuestionSeed("Bastiliya qachon olingan?", "1789", "1799", "1793", "1804"),
                new QuestionSeed("Ikkinchi jahon urushi boshlangan sana?", "1939-yil 1-sentyabr", "1941-yil 22-iyun", "1938-yil 30-sentyabr", "1940-yil 10-may"),
                new QuestionSeed("Qadimgi Yunoniston demokratiyasi beshigi?", "Afina", "Sparta", "Korinf", "Fiva")
        ));
        seedRealQuestions(sChet, 2.1, uzbek, List.of(
                new QuestionSeed("She ___ to school every day.", "goes", "go", "going", "gone"),
                new QuestionSeed("I have lived in Tashkent ___ 2010.", "since", "for", "in", "at"),
                new QuestionSeed("Synonym of 'Big'?", "Large", "Small", "Tiny", "Narrow"),
                new QuestionSeed("If I ___ rich, I would travel.", "were", "am", "was", "be"),
                new QuestionSeed("They ___ football now.", "are playing", "play", "played", "have played"),
                new QuestionSeed("Antonym of 'Hot'?", "Cold", "Warm", "Sunny", "Dry"),
                new QuestionSeed("___ you like coffee?", "Do", "Does", "Are", "Is"),
                new QuestionSeed("This is the ___ film I've seen.", "best", "good", "better", "well"),
                new QuestionSeed("I ___ my homework already.", "have finished", "finished", "finish", "finishing"),
                new QuestionSeed("What is ___ name?", "your", "you", "yours", "me")
        ));
        seedRealQuestions(sMatMajburiy, 1.1, uzbek, List.of(
                new QuestionSeed("Tenglamani yeching: 2x + 5 = 11", "3", "2", "4", "6"),
                new QuestionSeed("Hisoblang: sqrt(5^2 + 12^2)", "13", "17", "25", "169"),
                new QuestionSeed("Soddalashtiring: (a+b)^2 - (a-b)^2", "4ab", "2a^2+2b^2", "2ab", "0"),
                new QuestionSeed("15 ning 20% i?", "3", "2", "4", "5"),
                new QuestionSeed("x^2 - 9 = 0?", "3 va -3", "3", "-3", "0"),
                new QuestionSeed("1/2 + 1/4?", "3/4", "2/6", "1/6", "1/2"),
                new QuestionSeed("Uchburchak ichki burchaklari yig'indisi?", "180", "90", "360", "270"),
                new QuestionSeed("2^5?", "32", "16", "64", "10"),
                new QuestionSeed("log2(8)?", "3", "2", "4", "8"),
                new QuestionSeed("Doira yuzi formulasi?", "pi*r^2", "2*pi*r", "pi*d", "4*pi*r^2")
        ));
        seedRealQuestions(sOnaMajburiy, 1.1, uzbek, List.of(
                new QuestionSeed("Imlo xatosi bor so'z?", "Mexmon", "Mashhur", "Taajjub", "Jur'at"),
                new QuestionSeed("'Kitob - bilim manbai' - ega qaysi turkum?", "Ot", "Sifat", "Olmosh", "Fe'l"),
                new QuestionSeed("Yasama so'zlar qatori?", "Gulchi, bilim, ishchi", "Kitob, daftar, qalam", "Keldi, o'qidi, yozdi", "Bog', tog', qir"),
                new QuestionSeed("'Keldi' fe'li zamoni?", "O'tgan zamon", "Hozirgi zamon", "Kelasi zamon", "Buyruq mayli"),
                new QuestionSeed("Sifat darajalari nechta?", "3", "2", "4", "5"),
                new QuestionSeed("'Chiroyli' so'zi turkumi?", "Sifat", "Ot", "Son", "Ravish"),
                new QuestionSeed("Undoshlar nechta?", "23", "25", "24", "26"),
                new QuestionSeed("Unlilar nechta?", "6", "5", "7", "10"),
                new QuestionSeed("'Maktabga' so'zidagi kelshik?", "Jo'nalish", "Tushum", "Qaratqich", "O'rin-payt"),
                new QuestionSeed("'Men' olmoshi turi?", "Kishilik", "Ko'rsatish", "O'zlik", "So'roq")
        ));
        seedRealQuestions(sUzTarix, 1.1, uzbek, List.of(
                new QuestionSeed("Amir Temur tug'ilgan yili?", "1336", "1342", "1330", "1370"),
                new QuestionSeed("Mustaqillik e'lon qilingan kun?", "1991-yil 31-avgust", "1990-yil 20-iyun", "1991-yil 1-sentyabr", "1992-yil 8-dekabr"),
                new QuestionSeed("Xorazm poytaxti?", "Tuproqqal'a", "Varaxsha", "Poykent", "Afrosiyob"),
                new QuestionSeed("'Al-Qonun fit-tib' muallifi?", "Ibn Sino", "Al-Xorazmiy", "Beruniy", "Ulug'bek"),
                new QuestionSeed("Jaloliddin Manguberdi qaysi sulola vakili?", "Xorazmshohlar", "Temuriylar", "Somoniylar", "Qoraxoniylar"),
                new QuestionSeed("Birinchi Prezident?", "Islom Karimov", "Shavkat Mirziyoyev", "Yo'ldosh Oxunboboyev", "Sharof Rashidov"),
                new QuestionSeed("Konstitutsiya qabul qilingan sana?", "1992-yil 8-dekabr", "1991-yil 1-sentyabr", "1993-yil 10-dekabr", "1990-yil 24-mart"),
                new QuestionSeed("Buxoro amirligi qachon tugatilgan?", "1920", "1917", "1924", "1868"),
                new QuestionSeed("'Zij-i Jadidi Ko'ragoniy' muallifi?", "Mirzo Ulug'bek", "Al-Farg'oniy", "Al-Koshiy", "Beruniy"),
                new QuestionSeed("O'zbekiston BMTga qachon a'zo bo'lgan?", "1992", "1991", "1993", "1995")
        ));
    }

    private void seedRealQuestions(Subject subject, double points, MajorLang lang, List<QuestionSeed> questions) {
        if (!examQuestionRepository.findBySubjectId(subject.getId()).isEmpty()) {
            return;
        }
        for (QuestionSeed qs : questions) {
            ExamQuestion q = new ExamQuestion();
            q.setSubject(subject);
            q.setQuestionText(qs.text());
            q.setPoint(points);
            q.setMajorLang(lang);
            q = examQuestionRepository.save(q);

            saveAnswer(q, qs.correct(), true);
            saveAnswer(q, qs.w1(), false);
            saveAnswer(q, qs.w2(), false);
            saveAnswer(q, qs.w3(), false);
        }
    }

    private void saveAnswer(ExamQuestion question, String text, boolean isCorrect) {
        ExamAnswer answer = new ExamAnswer();
        answer.setQuestion(question);
        answer.setAnswerText(text);
        answer.setIsCorrect(isCorrect);
        examAnswerRepository.save(answer);
    }

    private record QuestionSeed(String text, String correct, String w1, String w2, String w3) {
    }
}
