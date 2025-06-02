package com.milou.spring_boot.exception;

public class UserNotFoundException extends Exception {
    public UserNotFoundException() {
        super("Cannot find user.");
    }

    public UserNotFoundException(Integer id) {
        super("Cannot find user with id = " + id + ".");
    }
}
