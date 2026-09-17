package com.acme.salary.common;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error payload returned for all handled exceptions. {@code details}
 * carries field-level validation messages when applicable.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
}
