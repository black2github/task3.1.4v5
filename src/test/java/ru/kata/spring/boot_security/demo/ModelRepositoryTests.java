package ru.kata.spring.boot_security.demo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.dao.DaoRepository;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.*;

import static org.springframework.test.util.AssertionErrors.*;

@SpringBootTest
class ModelRepositoryTests {
	private static final Logger log = LoggerFactory.getLogger(ModelRepositoryTests.class);

	private static final int RANGE = 10000;
	private static Random r = new Random();

	@Autowired
	private DaoRepository<User, Long> userDaoRepository;

	@Autowired
	private DaoRepository<Role, Long> roleDaoRepository;

	@Autowired
	//@Qualifier("userService")
	private UserService userService;

	@Autowired
	@Qualifier("userService")
	private UserDetailsService userDetailsService;

	// @Test
	//   void contextLoads() {
	// }

	@Test
	void createRole() {
		log.debug("createRole: <-");
		Role role = new Role("USER" + r.nextInt(1000));
		roleDaoRepository.save(role);
		assertTrue("Just created role not found", roleDaoRepository.existsById(role.getId()));
		roleDaoRepository.delete(role);
	}

	@Test
	void deleteRole() {
		log.debug("deleteRole: <-");
		Role role = new Role("USER" + r.nextInt(1000));
		roleDaoRepository.save(role);
		roleDaoRepository.delete(role);
		assertFalse("Role must not be found", roleDaoRepository.existsById(role.getId()));
	}

	@Test
	void findRoleByNameAndId() {
		log.debug("findByRoleMethods: <-");
		Role role = new Role("USER" + r.nextInt(1000));
		roleDaoRepository.save(role);
		Role role2 = roleDaoRepository.findByName(role.getName()).orElse(null);
		assertNotNull("Role not found by name", role2);
		role2 = roleDaoRepository.findById(role.getId()).orElse(null);
		assertNotNull("Role not found by id", role2);
		roleDaoRepository.delete(role);
	}

	//@Test
	void roleFindAll() {
		log.debug("roleFindAll: <-");
		roleDaoRepository.deleteAll();
		List<Role> roles = new ArrayList<>(
				Arrays.asList(
						new Role("USER" + r.nextInt(1000)),
						new Role("USER" + r.nextInt(1000)),
						new Role("USER" + r.nextInt(1000))
				));
		for (Role role: roles) {
			roleDaoRepository.save(role);
		}
		List<Role> list = new LinkedList<>();
		Iterable<Role> it = roleDaoRepository.findAll();
		for (Role role : it) {
			list.add(role);
		}
		assertEquals("Invalid number of role.", 3, list.size());
		roles.forEach(roleDaoRepository::delete);
	}

	@Test
		// @Transactional
	void createRoleAndUser() {
		log.debug("createRoleAndUser: <-");

		Role role1 = new Role("USER" + r.nextInt(1000));
		roleDaoRepository.save(role1);
		Role role2 = new Role("ADMIN" + r.nextInt(1000));
		roleDaoRepository.save(role2);

		User user = new User("user"+r.nextInt(RANGE), "password"+r.nextInt(RANGE));
		user.setRoles(new LinkedHashSet<Role>(Arrays.asList(role1, role2)));

		user = userDaoRepository.save(user);
		User user2 = userDaoRepository.findById(user.getId()).orElse(null);
		assertNotNull("User not found", user2);
		log.debug("createRoleAndUser: user2="+user2);
		assertTrue("Role " + role1.getName() + " not found", user2.getRoles().contains(role1));
		assertTrue("Role " + role2.getName() + " not found", user2.getRoles().contains(role2));

		user.getRoles().clear();

		//userService.update(user.getId(), user); // TODO после исправления нужно проверить именно это, а не userDaoRepository.save
		userDaoRepository.save(user);
		user = userDaoRepository.findById(user.getId()).orElse(null);
		assertNotNull("User must exist", user);
		assertTrue("Role Set must be empty", user.getRoles().isEmpty());
		// clean
		userDaoRepository.delete(user);
		roleDaoRepository.delete(role1);
		roleDaoRepository.delete(role2);
	}

	@Test
	// @Transactional // один из вариантов бороться с исключением ленивой инициализации.
	// org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role:
	// ru.kata.spring.boot_security.demo.model.User.roles, could not initialize proxy - no Session
	// Но не всегда применим в плане эффективности
	void createUser() {
		log.debug("createUser: <-");
		// without roles
		User user = new User("user@domen.ru"+r.nextInt(RANGE), "password"+r.nextInt(RANGE));
		user = userDaoRepository.save(user);
		assertNotNull("Id must be not empty", user.getId());
		assertTrue("User must exist", userDaoRepository.existsById(user.getId()));
		userDaoRepository.delete(user);

		// with roles
		Role role = new Role("ROLE" + r.nextInt(1000));
		roleDaoRepository.save(role);
		user = new User("user@domen.ru"+r.nextInt(RANGE), "password"+r.nextInt(RANGE));
		user.getRoles().add(role);
		user = userDaoRepository.save(user);
		assertTrue("User must exist", userDaoRepository.existsById(user.getId()));
		User user2 =  userDaoRepository.findById(user.getId()).orElse(null);
		assertNotNull("User must not be null.", user2);
		assertTrue("Role not in Roles set", user2.getRoles().remove(role));
		assertTrue("Roles set must be empty", user2.getRoles().isEmpty());

		// clean
		userDaoRepository.delete(user);
		roleDaoRepository.delete(role);
	}

	@Test
	void deleteUser() {
		User user = new User("user@domen.del"+r.nextInt(RANGE), "user"+r.nextInt(RANGE));
		Role role = new Role("ROLE_USER" + r.nextInt(RANGE));
		user.getRoles().add(role);
		roleDaoRepository.save(role);
		user = userDaoRepository.save(user);
		user = userDaoRepository.findById(user.getId()).orElse(null);
		assertNotNull("deleteUser: Just created user not found", user);

		userDaoRepository.delete(user);
		roleDaoRepository.delete(role);
		user = userDaoRepository.findById(user.getId()).orElse(null);
		assertNull("delete User: Found user after delete", user);
	}

	@Test
	void updateUser() {
		User user = new User("name@domen.up"+r.nextInt(RANGE), "passwd"+r.nextInt(RANGE));
		Role role = new Role("ROLE_USER" + r.nextInt(RANGE));
		user.getRoles().add(role);
		roleDaoRepository.save(role);
		user = userDaoRepository.save(user);

		User user2 = new User("name@domen.qu"+r.nextInt(RANGE), "second"+r.nextInt(RANGE), 10, "fistNameX", "lastNameX");
		Role role2 = new Role("ROLE_ADMIN"+r.nextInt(RANGE));
		Role role3 = new Role("ROLE_ADMIN"+r.nextInt(RANGE));
		user2.getRoles().add(role2);
		user2.getRoles().add(role3);
		roleDaoRepository.save(role2);
		roleDaoRepository.save(role3);
		log.debug("user2="+user2);

		user2.setId(user.getId());
		user = userService.update(user2);
		log.debug("user after update = "+user);

		User user3 = userDaoRepository.findById(user.getId()).orElse(null);
		log.debug("user3 = "+user3);
		log.debug("user3==user ? " + user3.equals(user));
		assertEquals("User not updated properly.", user3, user);

		// clean
		userDaoRepository.delete(user3);
		roleDaoRepository.delete(role);
		roleDaoRepository.delete(role3);
		roleDaoRepository.delete(role2);
	}

	@Test
	void loadUser() {
		User user = new User("name"+r.nextInt(RANGE), "password"+r.nextInt(RANGE));
		Role role = new Role("ROLE_USER"+r.nextInt(RANGE));
		user.getRoles().add(role);
		roleDaoRepository.save(role);
		user = userDaoRepository.save(user);

		UserDetails user2 = userDetailsService.loadUserByUsername(user.getEmail());
		assertEquals("usernames are not equal.", user, user2);

		// clean
		userDaoRepository.delete(user);
		roleDaoRepository.delete(role);
	}

	@Test
	void findUserByEmail() {
		User user = new User("name"+r.nextInt(RANGE), "password"+r.nextInt(RANGE));
		user = userDaoRepository.save(user);
		User user2 = userDaoRepository.findByName(user.getEmail()).orElse(null);
		assertNotNull("User not found by email.", user2);
		assertEquals("Users must be the same.", user, user2);
	}
}
