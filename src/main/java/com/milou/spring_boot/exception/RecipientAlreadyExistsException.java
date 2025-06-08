package com.milou.spring_boot.exception;

public class RecipientAlreadyExistsException extends RuntimeException {
    public RecipientAlreadyExistsException(int id) {
        super("Recipient already Exists in database. id = " + id);
    }
}
