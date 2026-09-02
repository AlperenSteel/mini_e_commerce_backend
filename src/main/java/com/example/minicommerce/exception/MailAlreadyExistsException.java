package com.example.minicommerce.exception;

public class MailAlreadyExistsException extends RuntimeException {
    public MailAlreadyExistsException(String message) {
        super(message);
    }
}
