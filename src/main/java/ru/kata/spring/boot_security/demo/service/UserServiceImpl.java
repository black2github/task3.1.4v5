package ru.kata.spring.boot_security.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kata.spring.boot_security.demo.*;
import ru.kata.spring.boot_security.demo.dao.DaoRepository;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service("userService")
@Repository
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final DaoRepository<Role, Long> roleDaoRepository;
    private final DaoRepository<User, Long> userDaoRepository;
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(DaoRepository<User, Long> userDaoRepository, DaoRepository<Role, Long> roleDaoRepository) {
        this.userDaoRepository = userDaoRepository;
        this.roleDaoRepository = roleDaoRepository;
    }

    @Autowired
    public void setPasswordEncoder(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User create(User user) {
        log.debug("create: <- " + user);
        if (user.getId() != null) {
            String errMsg = String.format("Not null (id=%d) user id present", user.getId());
            log.warn("create:" + errMsg);
            throw new InvalidFormatApplicationException(errMsg);
        }
        if (!user.getRoles().isEmpty()) {
            // make sure all roles have managed state
            // помним, что попытки модифицировать коллекцию во время итерирования по ней -> ConcurrentModificationException
            Set<Role> roles = new LinkedHashSet<>();
            for (Role role : user.getRoles()) {
                if (role.getId() == null) {
                    Role r = roleDaoRepository.findByName(role.getName()).orElse(null);
                    if (r == null) {
                        String errMsg = String.format("Unknown role %s", role.getName());
                        log.warn("create: " + errMsg);
                        throw new NotFoundApplicationException(errMsg);
                    }
                    // замена transient роли на managed и вставка в новый набор
                    roles.add(r);
                } else {
                    // перевод detached в managed и вставка в новый набор
                    roleDaoRepository.save(role);
                    roles.add(role);
                }
            }
            user.setRoles(roles);
        }
        if (userDaoRepository.findByName(user.getEmail()).isPresent()) {
            throw new SecurityApplicationException("Email already bound");
        }
        return userDaoRepository.save(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> listAll() {
        log.debug("listAll: <-");
        List<User> list = new LinkedList<>();
        Iterable<User> it = userDaoRepository.findAll();
        for (User user : it) {
            list.add(user);
        }
        log.debug("listAll: -> " + Arrays.toString(list.toArray()));
        return list;
    }

    @Transactional(readOnly = true)
    @Override
    public User find(Long id) {
        log.debug("find: <- id=" + id);
        User u = userDaoRepository.findById(id).orElse(null);
        if (u == null) {
            String errMsg = String.format("User with id=%s not found", id);
            log.warn("find: " + errMsg);
            throw new NotFoundApplicationException(errMsg);
        }
        return u;
    }

    @Override
    public void delete(User user) {
        log.debug("delete: <- " + user);
        userDaoRepository.delete(user);
    }

    @Override
    public void delete(Long id) {
        log.debug("delete: <- id=" + id);
        User usr = find(id);
        if (usr != null) {
            delete(usr);
        } else {
            String errMsg = "User with id=" + id + " not found";
            log.warn("delete: " + errMsg);
            throw new NotFoundApplicationException(errMsg);
        }
    }

    @Override
    public User update(User user) {
        log.debug("update: <- " + user);
        if (user.getId() == null) {
            throw new InvalidArgumentApplicationException("Not null user.id must be present");
        }
        return update(user.getId(), user);
    }

    /*
     * In case of LAZY roles attribute of User all roles must be in managed state.
     */
    private Set<Role> toManagedState(Set<Role> roles) {
        Set<Role> newRoles = new LinkedHashSet<>();
        if (roles == null || roles.isEmpty()) {
            return roles;
        }
        // заменять inplace при обходе нельзя - ConcurrentModificationException
        for (Role role : roles) {
            if (role == null) continue; // в сете может быть null в качестве ключа роли!
            if (role.getId() == null) {
                Role r = roleDaoRepository.findByName(role.getName()).orElse(null);
                if (r == null) {
                    String errMsg = String.format("Unknown role %s", role.getName());
                    log.warn("update: " + errMsg);
                    throw new ApplicationException(errMsg);
                }
                // замена transient роли на managed и вставка в новый набор
                newRoles.add(r);
            } else {
                // перевод detached в managed и вставка в новый набор
                roleDaoRepository.save(role);
                newRoles.add(role);
            }
        }
        return newRoles;
    }

    private User update(long id, User user) {
        log.debug("update: <- id=" + id);

        User u = userDaoRepository.findById(id).orElse(null);
        if (u == null) {
            String errMsg = "update: User with id=" + id + " not found";
            log.warn("update: " + errMsg);
            throw new NotFoundApplicationException(errMsg);
        }

        if (user.getEmail() != null) {
            u.setEmail(user.getEmail());
        }
        if (user.getPassword() != null) {
            u.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getAge() != 0) {
            u.setAge(user.getAge());
        }
        if (user.getFirstName() != null) {
            u.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            u.setLastName(user.getLastName());
        }
        u.setRoles(toManagedState(user.getRoles()));

        u = userDaoRepository.save(u);
        log.debug("update: -> " + u);
        return u;
    }

    @Override
    public boolean isUserExist(long id) {
        return userDaoRepository.existsById(id);
    }

    @Override
    public User findUserByEmail(String username) {
        log.debug("findUserByEmail: <- " + username);
        User user = userDaoRepository.findByName(username).orElse(null);
        log.debug("findUserByEmail: -> " + user);
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug(String.format("loadUserByUsername: <- username='%s'", username));
        User user = userDaoRepository.findByName(username).orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("User with username='" + username + "' not found.");
        }
        log.debug("loadUserByUsername: -> " + user);
        return user;
    }
}
