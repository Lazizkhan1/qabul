package uz.umft.qabul.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.dto.contract.ContractDto;
import uz.umft.qabul.entity.Contract;
import uz.umft.qabul.repository.ContractRepository;
import uz.umft.qabul.repository.ExamRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final ExamRepository examRepository;

    public List<ContractDto> findAll() {
        return contractRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public ContractDto findById(UUID id) {
        return contractRepository.findById(id).map(this::toDto).orElseThrow();
    }

    @Transactional
    public ContractDto create(ContractDto dto) {
        Contract contract = new Contract();
        mapToEntity(dto, contract);
        return toDto(contractRepository.save(contract));
    }

    @Transactional
    public ContractDto update(UUID id, ContractDto dto) {
        Contract contract = contractRepository.findById(id).orElseThrow();
        mapToEntity(dto, contract);
        return toDto(contractRepository.save(contract));
    }

    @Transactional
    public void delete(UUID id) {
        contractRepository.deleteById(id);
    }

    private ContractDto toDto(Contract c) {
        return ContractDto.builder()
                .id(c.getId())
                .contractUrl(c.getContractUrl())
                .examId(c.getExam() != null ? c.getExam().getId() : null)
                .scale(c.getScale())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private void mapToEntity(ContractDto dto, Contract c) {
        c.setContractUrl(dto.getContractUrl());
        c.setScale(dto.getScale());
        if (dto.getExamId() != null) {
            c.setExam(examRepository.findById(dto.getExamId()).orElseThrow());
        }
    }
}
