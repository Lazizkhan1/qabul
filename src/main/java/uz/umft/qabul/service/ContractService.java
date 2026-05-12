package uz.umft.qabul.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.umft.qabul.dto.contract.ContractDto;
import uz.umft.qabul.entity.Application;
import uz.umft.qabul.entity.Contract;
import uz.umft.qabul.entity.ExamSession;
import uz.umft.qabul.entity.User;
import uz.umft.qabul.enums.ExamSessionStatus;
import uz.umft.qabul.exception.AuthException;
import uz.umft.qabul.repository.ContractRepository;
import uz.umft.qabul.repository.ExamSessionRepository;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final ExamSessionRepository examSessionRepository;

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

    @Transactional
    public byte[] generateContractPdf(UUID sessionId, User user) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> AuthException.notFound("SESSION_NOT_FOUND", "Exam session not found"));

        Application app = session.getApplication();
        if (!app.getUser().getId().equals(user.getId())) {
            throw AuthException.forbidden("ACCESS_DENIED", "You do not have access to this contract");
        }

        if (session.getStatus() != ExamSessionStatus.COMPLETED) {
            throw AuthException.badRequest("EXAM_NOT_COMPLETED", "Exam is not completed yet");
        }

        contractRepository.findByExamSessionId(sessionId).orElseGet(() -> {
            Double score = session.getScore();
            if (score == null || score < 40.0) {
                throw AuthException.badRequest("INSUFFICIENT_SCORE", "Score is insufficient for contract generation");
            }
            double coefficient = score >= 56.4 ? 1.0 : 1.5;
            Contract c = new Contract();
            c.setExamSession(session);
            c.setScale(coefficient);
            return contractRepository.save(c);
        });


        try (InputStream template = new ClassPathResource("templates/CONTRACT_EXAMPLE.pdf").getInputStream();) {
            return template.readAllBytes();
        } catch (Exception e) {
            return new byte[0];
        }
    }


    private ContractDto toDto(Contract c) {
        return ContractDto.builder()
                .id(c.getId())
                .contractUrl(c.getContractUrl())
                .examSessionId(c.getExamSession() != null ? c.getExamSession().getId() : null)
                .scale(c.getScale())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private void mapToEntity(ContractDto dto, Contract c) {
        c.setContractUrl(dto.getContractUrl());
        c.setScale(dto.getScale());
        if (dto.getExamSessionId() != null) {
            c.setExamSession(examSessionRepository.findById(dto.getExamSessionId()).orElseThrow());
        }
    }
}
