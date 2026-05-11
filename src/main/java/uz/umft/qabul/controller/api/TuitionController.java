package uz.umft.qabul.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.dto.tuition.TuitionDto;
import uz.umft.qabul.service.TuitionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tuitions")
@RequiredArgsConstructor
public class TuitionController {

    private final TuitionService tuitionService;

    @GetMapping
    public List<TuitionDto> list(@RequestParam(required = false) String majorType,
                                 @RequestParam(required = false) String majorLang) {
        return tuitionService.findAll(majorType, majorLang);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TuitionDto create(@RequestBody TuitionDto tuition) {
        return tuitionService.create(tuition);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TuitionDto update(@PathVariable UUID id, @RequestBody TuitionDto tuition) {
        return tuitionService.update(id, tuition);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        tuitionService.delete(id);
    }
}
