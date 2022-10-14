package ru.kata.spring.boot_security.demo.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.model.Role;

import javax.persistence.EntityGraph;


@Component
public class RoleDaoRepository extends AbstractDaoRepository<Role, Long> {
    protected static final Logger log = LoggerFactory.getLogger(DaoRepository.class.getName());

    RoleDaoRepository() {
        super();
    }

    @Override
    protected String getEntityName() {
        return "Role";
    }

    @Override
    protected Class<Role> getObjectClass() {
        return Role.class;
    }

    @Override
    protected EntityGraph getFindEntityGraph() {
        return null;
    }

    @Override
    protected Long getEntityId(Role role) {
        return role.getId();
    }

    @Override
    protected String getNameFieldName() {
        return "name";
    }

    @Override
    protected String getJoinFetchInject() {
        return "";
    }
}
