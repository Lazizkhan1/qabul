package uz.umft.qabul.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.dto.tuition.TuitionDto;
import uz.umft.qabul.entity.Tuition;
import uz.umft.qabul.repository.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TuitionService {

    private final TuitionRepository tuitionRepository;
    private final MajorRepository majorRepository;
    private final MajorTypeRepository majorTypeRepository;
    private final MajorLangRepository majorLangRepository;
    private final SchoolYearRepository schoolYearRepository;

    public List<TuitionDto> findAll(String majorType, String majorLang) {
        List<Tuition> tuitions;
        if (majorType != null && majorLang != null) {
            tuitions = tuitionRepository.findAllByMajorType_TypeAndMajorLang_Lang(majorType, majorLang);
        } else if (majorType != null) {
            tuitions = tuitionRepository.findAllByMajorType_Type(majorType);
        } else if (majorLang != null) {
            tuitions = tuitionRepository.findAllByMajorLang_Lang(majorLang);
        } else {
            tuitions = tuitionRepository.findAll();
        }
        return tuitions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TuitionDto findById(UUID id) {
        return tuitionRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Tuition not found"));
    }

    @Transactional
    public TuitionDto create(TuitionDto dto) {
        Tuition tuition = new Tuition();
        mapToEntity(dto, tuition);
        return toDto(tuitionRepository.save(tuition));
    }

    @Transactional
    public TuitionDto update(UUID id, TuitionDto dto) {
        Tuition tuition = tuitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tuition not found"));
        mapToEntity(dto, tuition);
        return toDto(tuitionRepository.save(tuition));
    }

    @Transactional
    public void delete(UUID id) {
        tuitionRepository.deleteById(id);
    }

    private TuitionDto toDto(Tuition tuition) {
        return TuitionDto.builder()
                .id(tuition.getId())
                .majorCode(tuition.getMajorCode())
                .schoolYearTitle(tuition.getSchoolYear() != null ? tuition.getSchoolYear().getTitle() : null)
                .majorTitle(tuition.getMajor().getTitle())
                .majorTypeName(tuition.getMajorType().getType())
                .majorLangName(tuition.getMajorLang().getLang())
                .degree(tuition.getDegree())
                .amount(tuition.getAmount())
                .build();
    }

    private void mapToEntity(TuitionDto dto, Tuition tuition) {
        tuition.setMajorCode(dto.getMajorCode());
        if (dto.getSchoolYearTitle() != null) {
            tuition.setSchoolYear(schoolYearRepository.findByTitle(dto.getSchoolYearTitle())
                    .orElseThrow(() -> new RuntimeException("School year not found")));
        }
        tuition.setMajor(majorRepository.findByTitle(dto.getMajorTitle())
                .orElseThrow(() -> new RuntimeException("Major not found")));
        tuition.setMajorType(majorTypeRepository.findByType(dto.getMajorTypeName())
                .orElseThrow(() -> new RuntimeException("Major type not found")));
        tuition.setMajorLang(majorLangRepository.findByLang(dto.getMajorLangName())
                .orElseThrow(() -> new RuntimeException("Major lang not found")));
        tuition.setDegree(dto.getDegree());
        tuition.setAmount(dto.getAmount());
    }
}
