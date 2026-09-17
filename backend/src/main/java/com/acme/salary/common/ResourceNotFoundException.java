package com.acme.salary.common;

/** Thrown when a requested resource (employee, reference row) does not exist. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
