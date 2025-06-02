package com.milou.spring_boot.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(Integer id) {
        super("User already exists in database. id = " + id);
    }
}
