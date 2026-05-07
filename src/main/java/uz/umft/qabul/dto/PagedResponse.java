package uz.umft.qabul.dto;

import java.util.List;

public record PagedResponse<T>(
        int page,
        int limit,
        long total,
        List<T> data
) {
}
