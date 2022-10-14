package ru.kata.spring.boot_security.demo.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.model.User;

import javax.persistence.EntityGraph;

@Component
public class UserDaoRepository extends AbstractDaoRepository<User, Long> {
    private static final Logger log = LoggerFactory.getLogger(UserDaoRepository.class.getName());

    UserDaoRepository() {
        super();
    }

    @Override
    protected String getEntityName() {
        return "User";
    }

    @Override
    protected Class<User> getObjectClass() {
        return User.class;
    }

    @Override
    protected EntityGraph getFindEntityGraph() {
        return em.getEntityGraph("User.roles");
    }

    @Override
    protected Long getEntityId(User user) {
        return user.getId();
    }

    @Override
    protected String getNameFieldName() {
        return "email";
    }

    @Override
    protected String getJoinFetchInject() {
        // смотри раздел Hibernate 16.8.2. Explicit association joins +
        // 16.8.4 - A fetch join overrides the laziness of a given association, specifying that the association
        // should be fetched with a SQL join.
        return "left join fetch o.roles";
    }
}

