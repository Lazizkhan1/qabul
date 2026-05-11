package uz.umft.qabul.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.dto.reference.*;
import uz.umft.qabul.service.ReferenceService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reference")
@RequiredArgsConstructor
public class ReferenceController {

    private final ReferenceService referenceService;

    // Majors
    @GetMapping("/majors")
    public List<MajorDto> listMajors() { return referenceService.listMajors(); }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/majors")
    @ResponseStatus(HttpStatus.CREATED)
    public MajorDto createMajor(@RequestBody MajorDto major) { return referenceService.createMajor(major); }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/majors/{id}")
    public MajorDto updateMajor(@PathVariable Integer id, @RequestBody MajorDto major) {
        return referenceService.updateMajor(id, major);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/majors/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMajor(@PathVariable Integer id) { referenceService.deleteMajor(id); }

    // Major Types
    @GetMapping("/major-types")
    public List<MajorTypeDto> listMajorTypes() { return referenceService.listMajorTypes(); }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/major-types")
    @ResponseStatus(HttpStatus.CREATED)
    public MajorTypeDto createMajorType(@RequestBody MajorTypeDto type) { return referenceService.createMajorType(type); }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/major-types/{id}")
    public MajorTypeDto updateMajorType(@PathVariable Integer id, @RequestBody MajorTypeDto type) {
        return referenceService.updateMajorType(id, type);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/major-types/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMajorType(@PathVariable Integer id) { referenceService.deleteMajorType(id); }

    // Major Languages
    @GetMapping("/major-langs")
    public List<MajorLangDto> listMajorLangs() { return referenceService.listMajorLangs(); }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/major-langs")
    @ResponseStatus(HttpStatus.CREATED)
    public MajorLangDto createMajorLang(@RequestBody MajorLangDto lang) { return referenceService.createMajorLang(lang); }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/major-langs/{id}")
    public MajorLangDto updateMajorLang(@PathVariable Integer id, @RequestBody MajorLangDto lang) {
        return referenceService.updateMajorLang(id, lang);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/major-langs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMajorLang(@PathVariable Integer id) { referenceService.deleteMajorLang(id); }

    // School Years
    @GetMapping("/school-years")
    public List<SchoolYearDto> listSchoolYears() { return referenceService.listSchoolYears(); }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/school-years")
    @ResponseStatus(HttpStatus.CREATED)
    public SchoolYearDto createSchoolYear(@RequestBody SchoolYearDto year) { return referenceService.createSchoolYear(year); }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/school-years/{id}")
    public SchoolYearDto updateSchoolYear(@PathVariable Integer id, @RequestBody SchoolYearDto year) {
        return referenceService.updateSchoolYear(id, year);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/school-years/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSchoolYear(@PathVariable Integer id) { referenceService.deleteSchoolYear(id); }

    // Subjects
    @GetMapping("/subjects")
    public List<SubjectDto> listSubjects() { return referenceService.listSubjects(); }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/subjects")
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectDto createSubject(@RequestBody SubjectDto subject) { return referenceService.createSubject(subject); }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/subjects/{id}")
    public SubjectDto updateSubject(@PathVariable Integer id, @RequestBody SubjectDto subject) {
        return referenceService.updateSubject(id, subject);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/subjects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubject(@PathVariable Integer id) { referenceService.deleteSubject(id); }

    // Cert Categories
    @GetMapping("/cert-categories")
    public List<CertCategoryDto> listCertCategories() { return referenceService.listCertCategories(); }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/cert-categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CertCategoryDto createCertCategory(@RequestBody CertCategoryDto category) { return referenceService.createCertCategory(category); }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/cert-categories/{id}")
    public CertCategoryDto updateCertCategory(@PathVariable Integer id, @RequestBody CertCategoryDto category) {
        return referenceService.updateCertCategory(id, category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/cert-categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCertCategory(@PathVariable Integer id) { referenceService.deleteCertCategory(id); }
}
