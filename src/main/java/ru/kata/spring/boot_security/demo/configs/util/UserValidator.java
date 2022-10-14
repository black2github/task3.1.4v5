package ru.kata.spring.boot_security.demo.configs.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import org.apache.commons.validator.routines.EmailValidator;

/**
 * Проверка формата и ограничений пользователя
 */
@Component
public class UserValidator implements Validator {
    private static final Logger log = LoggerFactory.getLogger(UserValidator.class);

    private final UserService userService;


    @Autowired
    public UserValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        log.debug("validate: <- " + o);

        User user = (User) o;

        if (user.getEmail() == null || user.getEmail().isEmpty() || user.getEmail().isBlank()) {
            // поле, код ошибки, сообщение ошибки
            errors.rejectValue("email", "email", "Email can't be null or empty");
        } else {
            if (! isValidEmailAddress(user.getEmail())) {
                errors.rejectValue("email", "email", "Email address isn't valid");
            }
        }

        if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().isBlank()) {
            errors.rejectValue("password", "password", "Password can't be null or empty");
        }

        if (user.getAge() < 0 || user.getAge() > 150) {
            // поле, код ошибки, сообщение ошибки
            errors.rejectValue("age", "age", "Age must be between 0 to 150");
        }
    }

    private static boolean isValidEmailAddress(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
