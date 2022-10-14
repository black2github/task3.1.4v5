package ru.kata.spring.boot_security.demo;

public class NotFoundApplicationException extends ApplicationException {

    public NotFoundApplicationException() {
    }

    public NotFoundApplicationException(String err) {
        super(err);
    }

    public NotFoundApplicationException(String err, Throwable th) {
        super(err, th);
    }
}
