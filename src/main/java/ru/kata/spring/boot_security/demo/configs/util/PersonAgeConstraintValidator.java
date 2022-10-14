package ru.kata.spring.boot_security.demo.configs.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PersonAgeConstraintValidator implements ConstraintValidator<PersonAgeConstraint, Integer> {
    @Override
    public boolean isValid(Integer age, ConstraintValidatorContext constraintValidatorContext) {
        return age > -1;
    }
}