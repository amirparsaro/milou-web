package com.milou.spring_boot.exception;

public class MessageNotFoundException extends Exception {
    public MessageNotFoundException() {
        super("Cannot find Message.");
    }

    public MessageNotFoundException(Long id) {
        super("Cannot find Message with id = " + id + ".");
    }
}