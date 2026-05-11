package uz.umft.qabul.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.dto.reference.*;
import uz.umft.qabul.entity.*;
import uz.umft.qabul.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReferenceService {

    private final MajorRepository majorRepository;
    private final MajorTypeRepository majorTypeRepository;
    private final MajorLangRepository majorLangRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final SubjectRepository subjectRepository;
    private final CertCategoryRepository certCategoryRepository;

    // Major
    public List<MajorDto> listMajors() {
        return majorRepository.findAll().stream().map(m -> new MajorDto(m.getId(), m.getTitle())).collect(Collectors.toList());
    }
    @Transactional public MajorDto createMajor(MajorDto dto) {
        Major major = new Major(dto.getTitle());
        return toDto(majorRepository.save(major));
    }
    @Transactional public MajorDto updateMajor(Integer id, MajorDto dto) {
        Major major = majorRepository.findById(id).orElseThrow();
        major.setTitle(dto.getTitle());
        return toDto(majorRepository.save(major));
    }
    @Transactional public void deleteMajor(Integer id) { majorRepository.deleteById(id); }
    private MajorDto toDto(Major m) { return new MajorDto(m.getId(), m.getTitle()); }

    // MajorType
    public List<MajorTypeDto> listMajorTypes() {
        return majorTypeRepository.findAll().stream().map(m -> new MajorTypeDto(m.getId(), m.getType(), m.getActive())).collect(Collectors.toList());
    }
    @Transactional public MajorTypeDto createMajorType(MajorTypeDto dto) {
        MajorType majorType = new MajorType(dto.getType(), dto.getActive());
        return toDto(majorTypeRepository.save(majorType));
    }
    @Transactional public MajorTypeDto updateMajorType(Integer id, MajorTypeDto dto) {
        MajorType majorType = majorTypeRepository.findById(id).orElseThrow();
        majorType.setType(dto.getType());
        majorType.setActive(dto.getActive());
        return toDto(majorTypeRepository.save(majorType));
    }
    @Transactional public void deleteMajorType(Integer id) { majorTypeRepository.deleteById(id); }
    private MajorTypeDto toDto(MajorType m) { return new MajorTypeDto(m.getId(), m.getType(), m.getActive()); }

    // MajorLang
    public List<MajorLangDto> listMajorLangs() {
        return majorLangRepository.findAll().stream().map(m -> new MajorLangDto(m.getId(), m.getLang())).collect(Collectors.toList());
    }
    @Transactional public MajorLangDto createMajorLang(MajorLangDto dto) {
        MajorLang majorLang = new MajorLang(dto.getLang());
        return toDto(majorLangRepository.save(majorLang));
    }
    @Transactional public MajorLangDto updateMajorLang(Integer id, MajorLangDto dto) {
        MajorLang majorLang = majorLangRepository.findById(id).orElseThrow();
        majorLang.setLang(dto.getLang());
        return toDto(majorLangRepository.save(majorLang));
    }
    @Transactional public void deleteMajorLang(Integer id) { majorLangRepository.deleteById(id); }
    private MajorLangDto toDto(MajorLang m) { return new MajorLangDto(m.getId(), m.getLang()); }

    // SchoolYear
    public List<SchoolYearDto> listSchoolYears() {
        return schoolYearRepository.findAll().stream().map(m -> new SchoolYearDto(m.getId(), m.getTitle(), m.getActive())).collect(Collectors.toList());
    }
    @Transactional public SchoolYearDto createSchoolYear(SchoolYearDto dto) {
        SchoolYear schoolYear = new SchoolYear();
        schoolYear.setTitle(dto.getTitle());
        schoolYear.setActive(dto.getActive());
        return toDto(schoolYearRepository.save(schoolYear));
    }
    @Transactional public SchoolYearDto updateSchoolYear(Integer id, SchoolYearDto dto) {
        SchoolYear schoolYear = schoolYearRepository.findById(id).orElseThrow();
        schoolYear.setTitle(dto.getTitle());
        schoolYear.setActive(dto.getActive());
        return toDto(schoolYearRepository.save(schoolYear));
    }
    @Transactional public void deleteSchoolYear(Integer id) { schoolYearRepository.deleteById(id); }
    private SchoolYearDto toDto(SchoolYear m) { return new SchoolYearDto(m.getId(), m.getTitle(), m.getActive()); }

    // Subject
    public List<SubjectDto> listSubjects() {
        return subjectRepository.findAll().stream().map(m -> new SubjectDto(m.getId(), m.getTitle(), m.getExamDuration(), m.getTotalQuestions())).collect(Collectors.toList());
    }
    @Transactional public SubjectDto createSubject(SubjectDto dto) {
        Subject subject = new Subject();
        subject.setTitle(dto.getTitle());
        subject.setExamDuration(dto.getExamDuration());
        subject.setTotalQuestions(dto.getTotalQuestions());
        return toDto(subjectRepository.save(subject));
    }
    @Transactional public SubjectDto updateSubject(Integer id, SubjectDto dto) {
        Subject subject = subjectRepository.findById(id).orElseThrow();
        subject.setTitle(dto.getTitle());
        subject.setExamDuration(dto.getExamDuration());
        subject.setTotalQuestions(dto.getTotalQuestions());
        return toDto(subjectRepository.save(subject));
    }
    @Transactional public void deleteSubject(Integer id) { subjectRepository.deleteById(id); }
    private SubjectDto toDto(Subject m) { return new SubjectDto(m.getId(), m.getTitle(), m.getExamDuration(), m.getTotalQuestions()); }

    // CertCategory
    public List<CertCategoryDto> listCertCategories() {
        return certCategoryRepository.findAll().stream().map(m -> new CertCategoryDto(m.getId(), m.getTitle(), m.getType())).collect(Collectors.toList());
    }
    @Transactional public CertCategoryDto createCertCategory(CertCategoryDto dto) {
        CertCategory certCategory = new CertCategory(dto.getTitle(), dto.getType());
        return toDto(certCategoryRepository.save(certCategory));
    }
    @Transactional public CertCategoryDto updateCertCategory(Integer id, CertCategoryDto dto) {
        CertCategory certCategory = certCategoryRepository.findById(id).orElseThrow();
        certCategory.setTitle(dto.getTitle());
        certCategory.setType(dto.getType());
        return toDto(certCategoryRepository.save(certCategory));
    }
    @Transactional public void deleteCertCategory(Integer id) { certCategoryRepository.deleteById(id); }
    private CertCategoryDto toDto(CertCategory m) { return new CertCategoryDto(m.getId(), m.getTitle(), m.getType()); }
}
