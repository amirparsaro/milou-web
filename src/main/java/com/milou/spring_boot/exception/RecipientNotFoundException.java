package com.milou.spring_boot.exception;

public class RecipientNotFoundException extends Exception {
    public RecipientNotFoundException() {
        super("Cannot find Recipient.");
    }
    
    public RecipientNotFoundException(int id) {
        super("Cannot find Recipient with id = " + id + ".");
    }
}
