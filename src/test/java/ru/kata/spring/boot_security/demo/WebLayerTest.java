package ru.kata.spring.boot_security.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Errors;
import ru.kata.spring.boot_security.demo.configs.util.UserValidator;
import ru.kata.spring.boot_security.demo.controller.RestApiController;
import ru.kata.spring.boot_security.demo.dao.RoleDaoRepository;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.kata.spring.boot_security.demo.configs.AppURLs.API_USERS;

@WebMvcTest
public class WebLayerTest {

    @Autowired
    private MockMvc mockMvc;

    // заглушки бинов
    @MockBean
    private UserService userService;
    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @MockBean
    private RoleDaoRepository roleDaoRepository;
    @MockBean
    private UserValidator userValidator;

    @Test
    @WithUserDetails(value = "admin@a.b")
    public void shouldReturnUsersList() throws Exception {
        when(userService.listAll()).thenReturn(userList());  // задачи для сервисом
        doNothing().when(userValidator).validate(Object.class, Errors.class.newInstance()); // задачи для сервисом

        this.mockMvc.perform(get(API_USERS)).andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(jsonUserList()));
    }

    private List<User> userList() {
        Role role1 = new Role("USER");
        Role role2 = new Role("ADMIN");
        User user = new User("user@a.b", "user");
        User admin = new User("admin@a.b", "admin");
        user.setRoles(new LinkedHashSet<Role>(Arrays.asList(role1)));
        admin.setRoles(new LinkedHashSet<Role>(Arrays.asList(role1, role2)));

        ObjectMapper objectMapper = new ObjectMapper();
        return Arrays.asList(admin, user);
    }

    private String jsonUserList() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(userList());
    }
}
