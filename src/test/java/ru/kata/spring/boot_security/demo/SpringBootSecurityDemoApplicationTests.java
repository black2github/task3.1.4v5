package ru.kata.spring.boot_security.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.kata.spring.boot_security.demo.controller.RestApiController;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.kata.spring.boot_security.demo.configs.AppURLs.API_USERS;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
//@ExtendWith(SpringExtension.class)
class SpringBootSecurityDemoApplicationTests {
    private static final Logger log = LoggerFactory.getLogger(SpringBootSecurityDemoApplicationTests.class);

    @Autowired
    MockMvc mvc;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    RestApiController restApiController;

    @LocalServerPort
    private int port;

    ObjectMapper mapper = new ObjectMapper();
    private static Random r = new Random();

    // @BeforeEach
    // void setup(WebApplicationContext wac) {
    // 	this.mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    // }

    // @Test
    @BeforeEach
    void contextLoads() {

    }

    //
    // Test Anonymous Users
    //
    @Test
    @WithAnonymousUser
    public void whenAnonymousAccessLogin_thenOk() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void whenAnonymousAccessRoot_thenOk() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "user@a.ru")
    public void whenUserAccessAdminSecuredEndpoint_thenIsForbidden() throws Exception {
        mvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "admin@a.ru")
    public void whenAdminAccessAdminSecuredEndpoint_thenIsAllowed() throws Exception {
        mvc.perform(get("/admin"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "admin@a.ru")
    public void getUserList() throws Exception {
        MvcResult result = mvc.perform(get(API_USERS))
                .andExpect(status().isOk()).andExpect(content().contentType("application/json"))
                .andDo(print()).andReturn();
        User[] users = mapper.readValue(result.getResponse().getContentAsString(), User[].class);
        assertThat(users.length > 1);
    }

    @Test
    @WithUserDetails(value = "admin@a.ru")
    public void getUser() throws Exception {
        User u1 = new User("user@a.ru", "user");

        MvcResult result = mvc.perform(get(API_USERS + "/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();
        User user = mapper.readValue(result.getResponse().getContentAsString(), User.class);
        assertThat(user.equals(u1));
    }

    @Test
    @WithUserDetails(value = "admin@a.ru")
    public void deleteUserById() throws Exception {
        // создать дрозофилу
        User user = new User("user" + r.nextInt(1000) + "@a.ru", "user");
        String jsonUser = mapper.writeValueAsString(user);
        MvcResult result =
                mvc.perform(post(API_USERS).contentType(MediaType.APPLICATION_JSON).content(jsonUser))
                        .andExpect(status().is(HttpStatus.CREATED.value()))
                        .andReturn();
        user = mapper.readValue(result.getResponse().getContentAsString(), User.class);

        // удалить
        mvc.perform(delete(API_USERS + "/" + user.getId()))
                .andExpect(status().isOk());

        // попытка второго удаления должна провалиться со статусом 410
        mvc.perform(delete(API_USERS + "/" + user.getId()))
                .andExpect(status().is(HttpStatus.GONE.value()));
    }

    @Test
    @WithMockUser(username = "admin@a.ru", roles = {"USER", "ADMIN"})
    public void updateUserById() throws Exception {
        User user1 = new User("user" + r.nextInt(1000) + "@a.com", "user");

        // сначала создать
        String jsonUser = mapper.writeValueAsString(user1);
        MvcResult result =
                mvc.perform(post(API_USERS).contentType(MediaType.APPLICATION_JSON).content(jsonUser))
                        .andExpect(status().is(HttpStatus.CREATED.value()))
                        .andExpect(content().contentType("application/json"))
                        .andReturn();
        user1 = mapper.readValue(result.getResponse().getContentAsString(), User.class);

        // обновить
        user1.getRoles().add(new Role("USER"));
        user1.setFirstName("bla-bla");
        String url = API_USERS + "/" + user1.getId();
        result = mvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(user1)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();
        User user2 = mapper.readValue(result.getResponse().getContentAsString(), User.class);
        assertThat(user2.equals(user1));

        // то же проверить и через GET
        result = mvc.perform(get(API_USERS + "/" + user1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();
        user2 = mapper.readValue(result.getResponse().getContentAsString(), User.class);
        assertThat(user2.equals(user1));

        // clean
        mvc.perform(delete(API_USERS + "/" + user1.getId()));
    }
}
