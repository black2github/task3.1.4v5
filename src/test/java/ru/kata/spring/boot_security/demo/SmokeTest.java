package ru.kata.spring.boot_security.demo;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.kata.spring.boot_security.demo.controller.RestApiController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class SmokeTest {

    @Autowired
    private RestApiController restApiController;

    @Test
    void loadContext() {
        // проверка, что контроллер запускается
        assertThat(restApiController).isNotNull();
    }
}
