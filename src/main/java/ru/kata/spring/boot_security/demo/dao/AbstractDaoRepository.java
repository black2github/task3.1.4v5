package ru.kata.spring.boot_security.demo.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Transactional
public abstract class AbstractDaoRepository<T, ID> implements DaoRepository<T, ID> {
    protected static final Logger log = LoggerFactory.getLogger(AbstractDaoRepository.class.getName());

    @PersistenceContext
    protected EntityManager em;

    abstract protected String getEntityName();
    abstract protected Class<T> getObjectClass();
    abstract protected EntityGraph getFindEntityGraph();
    abstract protected ID getEntityId(T t);
    abstract protected String getNameFieldName();
    abstract protected String getJoinFetchInject();

    @Autowired
    AbstractDaoRepository() {
    }

    @Override
    public <S extends T> S save(S entity) {
        log.debug("save: <- " + entity);

        if (getEntityId(entity)==null) {
            em.persist(entity);
        } else {
            entity = em.merge(entity);
        }
        log.debug("save: -> " + entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        log.debug("findById: <- " + id);
        EntityGraph entityGraph = getFindEntityGraph();
        String query = String.format("select o from %s o %s where o.id = :id", getEntityName(), getJoinFetchInject());
        log.trace(String.format("findById: executing query = '%s' with :id='%s'", query, id));
        List<T> list = (List<T>) em.createQuery(query, getObjectClass())
                .setParameter("id", id)
                //.setHint("javax.persistence.fetchgraph", entityGraph)
                .getResultList();
        return Optional.ofNullable(list.isEmpty()? null: list.get(0));
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    @Override
    public Iterable<T> findAll() {
        log.debug("findAll: <- ");
        String query = String.format("select o from %s o", getEntityName());
        return em.createQuery(query, getObjectClass()).getResultList();
    }

    @Override
    public void deleteById(ID id) {
        log.debug("deleteById: <- " + id);
        String query = String.format("delete from %s where id = :id", getEntityName());
        int cnt = em.createQuery(query)
                .setParameter("id", id)
                .executeUpdate();
        String status = (cnt == 1) ? "deleted successfully" : "not found";
        log.debug("deleteById: -> User with id=" + id + " " + status);
    }

    @Override
    public void delete(T entity) {
        log.debug("delete: <- " + entity);
        if (entity == null) {
            log.warn("delete: user must not be null.");
            return;
        }
        deleteById(getEntityId(entity));
    }

    @Override
    public Optional<T> findByName(String name) {
        log.debug("findByName: <- " + name);
        EntityGraph entityGraph = getFindEntityGraph();
        String query = String.format("select o from %s o %s where o.%s = :name",
                getEntityName(), getJoinFetchInject(), getNameFieldName());
        List<T> list = em.createQuery(query, getObjectClass())
                .setParameter("name", name)
                //.setHint("javax.persistence.fetchgraph", entityGraph)
                .getResultList();
        T entity = (list.isEmpty() ? null : list.get(0));
        log.debug("findByName: -> " + entity);
        return Optional.ofNullable(entity);
    }

    @Override
    public void deleteAll() {
        log.debug("deleteAll: <- " );
        String query = String.format("delete from %s", getEntityName());
        em.createQuery(query).executeUpdate();
        log.debug("deleteAll: -> ");
    }

}

