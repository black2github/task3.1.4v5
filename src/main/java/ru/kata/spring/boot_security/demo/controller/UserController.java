package ru.kata.spring.boot_security.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /*
    GET /users/ - заполнение данных данных о конкретном пользователе для просмотра
     */
    @GetMapping()
    public String show(ModelMap model, Principal principal) {
        log.debug("show: <- name=" + principal.getName());

        // получение одного пользователя по id и передача на отображение
        User user = userService.findUserByEmail(principal.getName());

        List<User> users = new ArrayList<>(1);
        users.add(user);
        model.addAttribute("users", users);

        log.debug("show: -> " + user);
        return "user/user_panel";
    }
}
