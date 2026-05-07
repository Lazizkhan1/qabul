package uz.umft.qabul.controller.dto.application;

import java.util.List;

public record ApplicationListResponse(
        int page,
        int limit,
        long total,
        List<ApplicationResponse> data
) {
}
