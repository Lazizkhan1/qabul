package uz.umft.qabul.service;

import org.springframework.stereotype.Service;
import uz.umft.qabul.controller.dto.application.ApplicationResponse;
import uz.umft.qabul.entity.Application;
import uz.umft.qabul.entity.Cert;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApplicationMapper {

    public ApplicationResponse toResponse(Application application, List<Cert> certificates) {
        return new ApplicationResponse(
                application.getId(),
                application.getUser().getId(),
                application.getTuition().getId(),
                application.getStatus(),
                application.getFirstname(),
                application.getLastname(),
                application.getMiddlename(),
                application.getBirthDate(),
                application.getGender() == null ? null : application.getGender().toString(),
                application.getJshshir(),
                application.getPassportSeries(),
                application.getAddress(),
                application.getAdditionalPhone(),
                application.getDisability(),
                certificates.stream().map(this::toCertificateResponse).toList(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }

    public Map<UUID, List<Cert>> groupByApplicationId(List<Cert> certificates) {
        return certificates.stream().collect(Collectors.groupingBy(cert -> cert.getApplication().getId()));
    }

    private ApplicationResponse.CertificateResponse toCertificateResponse(Cert cert) {
        return new ApplicationResponse.CertificateResponse(
                cert.getId(),
                cert.getCertNumber(),
                cert.getScore(),
                cert.getFileUrl(),
                cert.getCategory().getId()
        );
    }
}
