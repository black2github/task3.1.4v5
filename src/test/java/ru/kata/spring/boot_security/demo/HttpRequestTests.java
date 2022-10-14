package ru.kata.spring.boot_security.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.kata.spring.boot_security.demo.configs.AppURLs.API_USERS;

// полномасштабный прогон с запуском всего сервера
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HttpRequestTests {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestTests.class);

    @LocalServerPort
    private int port;

    // TestRestTemplate provides a constructor with which we can create a template with specified
    // credentials for basic authentication.
    @Autowired
    private TestRestTemplate restTemplate;

    private static Random r = new Random();

    private String cookie = null;
    private String username = "admin@a.b";
    private String password = "admin";
    // ставь здесь то, что требуется - "http://localhost:" + port + "/login"
    private String loginUrlPrefix = "http://localhost:";
    private String loginUrlSuffix = "/login";

    private HttpHeaders headers = new HttpHeaders();

    @BeforeEach
    private void setCookie() {
        String loginUrl = loginUrlPrefix + port + loginUrlSuffix;
        log.debug(String.format("setCookie: <- cookie='%s', user='%s', password='%s', loginUrl='%s'",
                cookie, username, password, loginUrl));
        if (this.cookie == null) {
            headers = new HttpHeaders();
            // так отправляются данные формы
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.set("username", this.username);
            form.set("password", this.password);
            ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                    loginUrl,
                    new HttpEntity<>(form, new HttpHeaders()),
                    String.class);
            this.cookie = loginResponse.getHeaders().get("Set-Cookie").get(0);
            headers.add("Cookie", this.cookie);
            log.debug("setCookie: -> cookie = " + this.cookie);
        }
        else {
            log.debug("setCookie: -> .");
        }
    }

    // private String getCookieForUser(String username, String password, String loginUrl) {
    //     // так отправляются данные формы
    //     MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    //     form.set("username", username);
    //     form.set("password", password);
    //     ResponseEntity<String> loginResponse = restTemplate.postForEntity(
    //             loginUrl,
    //             new HttpEntity<>(form, new HttpHeaders()),
    //             String.class);
    //     String cookie = loginResponse.getHeaders().get("Set-Cookie").get(0);
    //     return null;
    // }

    @AfterEach
    public void delCookie() {
        this.cookie = null;
    }

    @Test
    public void deleteUser() throws Exception {
        String url = "http://localhost:" + port + API_USERS;
        User user = new User("new_user" + r.nextInt(1000) + "@a.com", "user");

        // создание пользователя
        RequestEntity<User> postReqEnt = new RequestEntity<>(user, headers, HttpMethod.POST, new URI(url));
        ResponseEntity<User> postRespEnt = this.restTemplate.exchange(postReqEnt, User.class);
        assertThat(postRespEnt.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = postRespEnt.getBody().getId();

        // удаление пользователя
        // TODO форма больше не нужна, переделать на удаление по /users/:id
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.set("id", id.toString());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity httpEnt = new HttpEntity<>(form, headers);
        ResponseEntity<String> delRespEnt = this.restTemplate.exchange(
                url +"/" + id, HttpMethod.DELETE, httpEnt, String.class, "");
        assertThat(delRespEnt.getStatusCode()).isEqualTo(HttpStatus.OK);

        // проверка, что пользователя более нет
        String showUrl = "http://localhost:" + port + API_USERS + "/" + id;
        ResponseEntity<User> getRespEnt = restTemplate.exchange(showUrl, HttpMethod.GET, new HttpEntity<>(headers), User.class);
        assertThat(getRespEnt.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void createUser() throws Exception {
        String url = "http://localhost:" + port + API_USERS;
        User user = new User("new_user" + r.nextInt(1000) + "@a.com", "user");

        // пользователь успешно добавлен
        // assertThat(this.restTemplate.postForObject(url, user, User.class)).isEqualTo(user);
        RequestEntity<User> postReqEnt = new RequestEntity<>(user, headers, HttpMethod.POST, new URI(url));
        ResponseEntity<User> postRespEnt = this.restTemplate.exchange(postReqEnt, User.class);
        assertThat(postRespEnt.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void createUser_rejectDuplicate_delete() throws Exception {
        String url = "http://localhost:" + port + API_USERS;
        User user = new User("new_user@a.ru", "user");

        // пользователь успешно добавлен
        // assertThat(this.restTemplate.postForObject(url, user, User.class)).isEqualTo(user);
        RequestEntity<User> postReqEnt = new RequestEntity<>(user, headers, HttpMethod.POST, new URI(url));
        ResponseEntity<User> postRespEnt = this.restTemplate.exchange(postReqEnt, User.class);
        assertThat(postRespEnt.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = postRespEnt.getBody().getId();

        // второй вызов -> дубликатный пользователь
        // HttpHeaders headers = new HttpHeaders();
        postReqEnt = new RequestEntity<>(user, headers, HttpMethod.POST, new URI(url));
        postRespEnt = this.restTemplate.exchange(postReqEnt, User.class);
        assertThat(postRespEnt.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // второй вызов -> дубликатный пользователь
        // HttpHeaders headers = new HttpHeaders();
        postReqEnt = new RequestEntity<>(null, headers, HttpMethod.DELETE, new URI(url+"/"+id));
        ResponseEntity<String> deleteRespEnt = this.restTemplate.exchange(postReqEnt, String.class);
        assertThat(deleteRespEnt.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // похоже из-за того, что просто возвращает страницу авторизации при необходимости.
    // То есть нужно проверять по содержимому
    // Здесь рассмотрим случай, когда тестируемый /url защищен, и с первого раза запрос с TestRestTemplate сделать не получится.
    // В этом случае отправляем два запроса — один для получения заголовка авторизации, а второй уже с целью тестирования.
    // похоже работает только для стандартной/встроенной страницы авторизации (которая возвращается
    // DefaultLoginPageGeneratingFilter)
    // @Test
    // @WithUserDetails(value = "admin@a.b")
    public void authorizeSecureRequestWithInternalLoginForm() {
        // В этом случае отправляем два запроса — один для получения заголовка авторизации/cookei,
        // а второй уже с целью тестирования метода.
        String loginUrl = "http://localhost:" + port + "/login";
        String url = "http://localhost:" + port + "/admin";

        log.debug("URL = " + url);
        restTemplate = new TestRestTemplate("admin@a.b", "admin",
                TestRestTemplate.HttpClientOption.ENABLE_COOKIES);

        // прохождение логина и скрытое получение cokies - не работает, не смотря на описание
        // https://www.baeldung.com/spring-boot-testresttemplate
        // похоже, работает только в случае стандартной/встроенной в SpringSecurity формы, а у нас - кастомная
        // ResponseEntity<String> response = restTemplate.getForEntity(loginUrl, String.class);
        ResponseEntity<String> response = restTemplate.getForEntity(loginUrl, String.class); // прохождение логина и скрытое получение cokies
        // другой вариант, когда не создаем сами TestRestTemplate
        // ResponseEntity<String> response = restTemplate.withBasicAuth("admin@a.b", "admin").getForEntity(url, String.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
        log.debug("authorizeSecureRequestWithInternalLoginForm:" + response.getBody().toString());
        Assertions.assertTrue(response.getBody().indexOf("Sign in") == -1); // 'Sign in' найден
    }

    @Test
    public void authorizeSecureRequestWithCustomLoginForm() {
        String url = "http://localhost:" + port + "/admin";

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
        //log.trace("authorizeSecureRequestWithCustomLoginForm:" + response.getBody().toString());
        Assertions.assertTrue(response.getBody().indexOf("Sign in") == -1); // 'Sign in' не найден
    }

    // @Test
    // public void createUser2() throws URISyntaxException {
    //     String url = "http://localhost:" + port + API_USERS;
    //
    //     User user = new User("user@b.c", "user");
    //     RequestEntity<User> postReqEnt = new RequestEntity<>(user, headers, HttpMethod.POST, new URI(url));
    //     ResponseEntity<User> postRespEnt = this.restTemplate.exchange(postReqEnt, User.class);
    //     assertThat(postRespEnt.getStatusCode()).isEqualTo(HttpStatus.OK);
    //     assertThat(postRespEnt.getBody()).isEqualTo(user);
    // }

    @Test
    public void getUserList() {
        String url = "http://localhost:" + port + API_USERS;
        ResponseEntity<User[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), User[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Arrays.stream(response.getBody()).count()).isGreaterThan(1);
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

    private String postMethod(User user, String url) throws URISyntaxException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        URI uri = new URI(url);
        RequestEntity<User> postReqEnt = new RequestEntity<>(user, headers, HttpMethod.POST, uri);
        ResponseEntity<String> postRespEnt = restTemplate.exchange(postReqEnt, String.class);
        return postRespEnt.getBody();
    }
}
