package ru.kata.spring.boot_security.demo;

public class SecurityApplicationException extends ApplicationException {

    public SecurityApplicationException() {
    }

    public SecurityApplicationException(String err) {
        super(err);
    }

    public SecurityApplicationException(String err, Throwable th) {
        super(err, th);
    }
}
