package ru.kata.spring.boot_security.demo.configs;

public class AppURLs {
    // version
    public static final String VERSION = "v1";
    // GET /api/admin/v1/users - получение списка пользователей
    public static final String API_ADMIN = "/api/admin/" + VERSION;
    // GET /api/admin/v1/users/{id} - получение данных конкретного пользователя
    public static final String USERS = "/users";
    public static final String API_USERS = API_ADMIN + USERS;

    // POST /api/admin/v1/users - создание пользователя
    // PATCH /api/admin/v1/users/{id} - обновление данных пользователя
    // DELETE /api/admin/v1/users/{id}  - удвление пользователя
    public static final String ADMIN = "/admin";
    public static final String USER = "/user";
}
