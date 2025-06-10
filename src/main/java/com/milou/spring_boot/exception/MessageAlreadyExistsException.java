package com.milou.spring_boot.exception;

public class MessageAlreadyExistsException extends RuntimeException {
    public MessageAlreadyExistsException(int id) {
        super("Message already Exists in database. id = " + id);
    }
}
