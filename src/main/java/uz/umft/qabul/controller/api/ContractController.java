package uz.umft.qabul.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.umft.qabul.dto.contract.ContractDto;
import uz.umft.qabul.service.ContractService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    public List<ContractDto> list() {
        return contractService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContractDto create(@RequestBody ContractDto contract) {
        return contractService.create(contract);
    }

    @PutMapping("/{id}")
    public ContractDto update(@PathVariable UUID id, @RequestBody ContractDto contract) {
        return contractService.update(id, contract);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        contractService.delete(id);
    }
}
