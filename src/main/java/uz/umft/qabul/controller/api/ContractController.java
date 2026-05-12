package uz.umft.qabul.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.dto.contract.ContractDto;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.service.ContractService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ContractDto> list() {
        return contractService.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ContractDto create(@RequestBody ContractDto contract) {
        return contractService.create(contract);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ContractDto update(@PathVariable UUID id, @RequestBody ContractDto contract) {
        return contractService.update(id, contract);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        contractService.delete(id);
    }

    @GetMapping("/download/{sessionId}")
    @PreAuthorize("hasRole('APPLICANT')")
    public ResponseEntity<byte[]> download(@PathVariable UUID sessionId, @AuthenticationPrincipal User user) {
        byte[] pdf = contractService.generateContractPdf(sessionId, user);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "contract.pdf");

        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
