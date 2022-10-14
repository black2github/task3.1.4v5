package ru.kata.spring.boot_security.demo.dao;

import ru.kata.spring.boot_security.demo.model.Role;

import java.util.Optional;

public interface DaoRepository<T, ID> {

    <S extends T> S save(S entity);

    Optional<T> findById(ID id);

    boolean existsById(ID id);

    Iterable<T> findAll();

    void deleteById(ID id);

    void delete(T entity);

    Optional<T> findByName(String name);

    void deleteAll();
}
