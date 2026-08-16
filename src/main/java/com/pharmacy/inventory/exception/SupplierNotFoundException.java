package com.pharmacy.inventory.exception;

public class SupplierNotFoundException extends RuntimeException {

    public SupplierNotFoundException(String message) {
        super(message);
    }

    public SupplierNotFoundException(Long id) {
        super("Supplier not found with id: " + id);
    }

    public SupplierNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SupplierNotFoundException(String field, String value) {
        super("Supplier with " + field + " '" + value + "' not found");
    }
}