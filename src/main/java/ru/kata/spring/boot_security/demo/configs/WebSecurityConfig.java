package ru.kata.spring.boot_security.demo.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;
import ru.kata.spring.boot_security.demo.configs.util.HeadersSpyFilter;

import static ru.kata.spring.boot_security.demo.configs.AppURLs.API_ADMIN;
import static ru.kata.spring.boot_security.demo.configs.AppURLs.API_USERS;

@Configuration
// "включает" Spring Security - добавляется DelegatingFilterProxy, задача которого заключается в том,
// чтобы вызвать цепочку фильтров (FilterChainProxy) из Spring Security.
@EnableWebSecurity
// включает глобальный метод безопасности.
// @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(WebSecurityConfig.class);

    private final SuccessUserHandler successUserHandler;

    public WebSecurityConfig(SuccessUserHandler successUserHandler) {
        this.successUserHandler = successUserHandler;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder,
                                             @Qualifier("userService") UserDetailsService userDetailsService)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder)
                .and()
                .build();
    }

    @Bean
    public SpringTemplateEngine templateEngine(ITemplateResolver templateResolver) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.addDialect(new SpringSecurityDialect());
        return templateEngine;
    }

    //
    // Configure HTTP Security:
    //
    // The HTTP security will build a DefaultSecurityFilterChain object to load request matchers and filters.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HeadersSpyFilter headersSpyFilter) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers(API_USERS +"/**").authenticated()
                .antMatchers("/", "/scripts/**", "/index", API_ADMIN +"/**").permitAll()
                // 4. Все CRUD-операции и страницы для них должны быть доступны только пользователю с ролью admin
                // по url: /admin/.
                .antMatchers("/admin/**").hasRole("ADMIN")
                //.antMatchers("/admin/**").hasAuthority("ADMIN")
                // .antMatchers("/admin/**").hasAnyRole("ADMIN", "USER")
                // 5. Пользователь с ролью user должен иметь доступ только к своей домашней странице /user, где выводятся
                // его данные. Доступ к этой странице должен быть только у пользователей с ролью user и admin.
                // Не забывайте про несколько ролей у пользователя!
                .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                .anyRequest().authenticated()
                .and().formLogin()
                    .loginPage("/login") // configures a custom login page (with custom controller) at URL "/login".
                        .successHandler(successUserHandler)
                            .permitAll()
                .and().logout().logoutSuccessUrl("/login").permitAll();

        http.httpBasic(); // добавлено для поддержки модификации через POSTMAN с обеспечениям security
        http.addFilterAfter(headersSpyFilter, BasicAuthenticationFilter.class);

        return http.build();
    }

    // аутентификация inMemory
    // @Bean
    // @Override
    // public UserDetailsService userDetailsService() {
    //     UserDetails user =
    //             User.withDefaultPasswordEncoder()
    //                     .username("user")
    //                     .password("user")
    //                     .roles("USER")
    //                     .build();
    //
    //     return new InMemoryUserDetailsManager(user);
    // }
}