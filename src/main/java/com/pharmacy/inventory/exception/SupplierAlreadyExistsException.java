package com.pharmacy.inventory.exception;

public class SupplierAlreadyExistsException extends RuntimeException {

    public SupplierAlreadyExistsException(String message) {
        super(message);
    }

    public SupplierAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public SupplierAlreadyExistsException(String field, String value) {
        super("Supplier with " + field + " '" + value + "' already exists");
    }

    public SupplierAlreadyExistsException(String field, String value, Long existingId) {
        super("Supplier with " + field + " '" + value + "' already exists with id: " + existingId);
    }
}