package ru.kata.spring.boot_security.demo;

public class InvalidArgumentApplicationException extends ApplicationException {

    public InvalidArgumentApplicationException() {
    }

    public InvalidArgumentApplicationException(String err) {
        super(err);
    }

    public InvalidArgumentApplicationException(String err, Throwable th) {
        super(err, th);
    }
}
