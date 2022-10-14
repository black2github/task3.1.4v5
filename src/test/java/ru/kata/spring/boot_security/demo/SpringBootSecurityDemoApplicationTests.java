package ru.kata.spring.boot_security.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import ru.kata.spring.boot_security.demo.controller.RestApiController;
import ru.kata.spring.boot_security.demo.model.User;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

	// @BeforeEach
	// public void setup() {
	// 	WebApplicationContext context =
	//
	// 	mvc = MockMvcBuilders
	// 			.webAppContextSetup(context)
	// 			.apply(springSecurity())
	// 			.build();
	// }

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
	@WithAnonymousUser
	public void whenAnonymousAccessRestrictedEndpoint_thenIsUnauthorized() throws Exception {
		mvc.perform(get("/user"))
				.andExpect(status().is3xxRedirection());
	}

	//
	// Test Admin Role
	//
	// @Test
	// @WithUserDetails(value = "user@a.b")
	// public void whenUserAccessUserSecuredEndpoint_thenOk() throws Exception {
	// 	mvc.perform(get("/user"))
	// 			.andExpect(status().isOk());
	// }

	@Test
	@WithUserDetails(value = "user@a.b")
	public void whenUserAccessAdminSecuredEndpoint_thenIsForbidden() throws Exception {
		mvc.perform(get("/admin"))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails(value = "admin@a.b")
	public void whenAdminAccessAdminSecuredEndpoint_thenIsAllowed() throws Exception {
		mvc.perform(get("/admin"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(value = "admin@a.b")
	// @WithUserDetails(value = "user@a.b")
	public void getUserList() throws Exception {
		User u1 = new User("user@a.b", "user");
		User u2 = new User("admin@a.b", "admin");
		List<User> users = Arrays.asList(u1, u2);

		mvc.perform(get(API_USERS))
				.andExpect(status().isOk()).andExpect(content().contentType("application/json"));

		// MvcResult result = mvc.perform(get(API_USERS + "/1"))
		// 		.andExpect(status().isOk())
		// 				// .andExpect(content().contentType("application/json")
		// 						.andReturn();
		// User user = mapper.readValue(result.getResponse().getContentAsString(), User.class);
		// assertThat(user.getEmail().equals(u1.getEmail()));
	}

	@Test
	@WithUserDetails(value = "admin@a.b")
	// @WithUserDetails(value = "user@a.b")
	public void getUser() throws Exception {
		User u1 = new User("user@a.b", "user");

		MvcResult result = mvc.perform(get(API_USERS + "/1"))
				.andExpect(status().isOk())
				// .andExpect(content().contentType("application/json")
				.andReturn();
		User user = mapper.readValue(result.getResponse().getContentAsString(), User.class);
		assertThat(user.equals(u1));
	}

	@Test
	@WithUserDetails(value = "admin@a.b")
	// @WithUserDetails(value = "user@a.b")
	public void deleteUserById() throws Exception {
		User u1 = new User("user@a.b", "user");

		mvc.perform(delete(API_USERS + "/1"))
				.andExpect(status().isOk());

		// попытка второго удаления должна провалиться со статусом 410
		mvc.perform(delete(API_USERS + "/1"))
				.andExpect(status().is(HttpStatus.GONE.value()));
	}

	@Test
	// @WithMockUser("admin")
	@WithMockUser(username="admin@a.b", roles={"USER","ADMIN"})
	//@WithUserDetails(value = "admin@a.b")
	// @WithUserDetails(value = "user@a.b")
	public void updateUserById() throws Exception {

		User u1 = new User("user@a.b" + r.nextInt(1000), "user");
		u1.setId(1l);

		mvc.perform(patch(API_USERS + "/" + u1.getId(), u1))
				.andExpect(status().isOk());

		MvcResult result = mvc.perform(get(API_USERS + "/" + u1.getId()))
				.andExpect(status().isOk())
				.andReturn();
		User user = mapper.readValue(result.getResponse().getContentAsString(), User.class);
		assertThat(user.equals(u1));
	}

	private User[] getRestUserList() throws URISyntaxException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// URI uri = new URI("http://localhost:8080/rest");
		// RequestEntity<String> getReqEnt = new RequestEntity<>(null, headers, HttpMethod.GET, uri);
		// ResponseEntity<User[]> getRespEnt = restTemplate.exchange(getReqEnt, User[].class);
		// return getRespEnt.getBody();
		return null;
	}

	// @Test
	// public void contextLoads() throws Exception {
	// 	assertThat(controller).isNotNull();
	// }
	//
	// @Test
	// public void greetingShouldReturnDefaultMessage() throws Exception {
	// 	assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/rest",
	// 			User[].class)).contains(..."Hello, World");
	// }
}
