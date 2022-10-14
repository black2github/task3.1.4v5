package ru.kata.spring.boot_security.demo;

public class InvalidFormatApplicationException extends ApplicationException {

    public InvalidFormatApplicationException() {
    }

    public InvalidFormatApplicationException(String err) {
        super(err);
    }

    public InvalidFormatApplicationException(String err, Throwable th) {
        super(err, th);
    }
}
