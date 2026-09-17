package com.acme.salary.common;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * A stable, minimal pagination envelope for list endpoints. Avoids leaking
 * Spring Data's internal {@code Page} serialization shape to API clients.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <S, T> PageResponse<T> from(Page<S> page, List<T> mappedContent) {
        return new PageResponse<>(
                mappedContent,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
