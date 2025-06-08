package com.milou.spring_boot.exception;

public class InvalidRegistrationException extends Exception {
    public InvalidRegistrationException(String message) {
        super(message);
    }
}
