package net.codjo.security.common;

public class BadLoginException extends Exception {
    public BadLoginException(String message, Throwable cause) {
        super(message, cause);
    }


    public BadLoginException(String message) {
        super(message);
    }
}