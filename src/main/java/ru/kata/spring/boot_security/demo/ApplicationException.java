package ru.kata.spring.boot_security.demo;

public class ApplicationException extends RuntimeException {

    public ApplicationException() {
        super();
    }

    public ApplicationException(String err) {
        super(err);
    }

    public ApplicationException(String err, Throwable th) {
        super(err, th);
    }
}
