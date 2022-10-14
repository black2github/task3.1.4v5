package ru.kata.spring.boot_security.demo.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.kata.spring.boot_security.demo.configs.util.PersonAgeConstraint;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@NamedEntityGraph(name = "User.roles",
        attributeNodes = @NamedAttributeNode("roles")
)
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uc_user_email", columnNames = {"email"})
})
@JsonSerialize(using = CustomUserSerializer.class)
@Getter
@Setter
//@RequiredArgsConstructor
public class User implements UserDetails {
    private static final Logger log = LoggerFactory.getLogger(User.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "email")
    @NotNull
    @NotEmpty(message = "Email should not be empty")
    private String email;

    @Column(name = "password")
    @NotNull
    @NotEmpty(message = "Password should not be empty")
    private String password;

    @PersonAgeConstraint
    private int age;

    @ManyToMany
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "User_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "roles_id", referencedColumnName = "id"))
    private Set<Role> roles = new LinkedHashSet<>();

    public User() {
    }

    public User(String email, String password, int age) {
        this(email, password, age, null, null);
    }

    public User(String email, String password) {
        this(email, password, 0, null, null);
    }

    public User(String email, String password, int age, String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.age = age;
    }

    // UserDetails support
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        log.debug("getAuthorities: ->" + mapRolesToAuthorities(roles));
        return mapRolesToAuthorities(roles);
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles) {
        // уровень аутентификации роли выделяет через наличиет префикса "ROLE_"
        return roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName())).collect(Collectors.toList());
        // return roles.stream().map(r -> new SimpleGrantedAuthority("" + r.getName())).collect(Collectors.toList());
    }

    public List<String> getRoleNames() {
        return roles.stream().map(r -> r.getName()).collect(Collectors.toList());
    }


    @Override
    public String toString() {
        return String.format("User{id=%d, email='%s', password='%s', age=%d, firstName='%s', lastName='%s', roles=%s}",
                id, email, password, age, firstName, lastName, roles == null ? "''" : Arrays.toString(roles.toArray()));
    }

    @Override
    public int hashCode() {
        return
                Objects.hash(id, email, password, age, firstName, lastName, roles);
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (o.getClass() != User.class)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        return (email.equals(((User) o).getEmail())
                && password.equals(((User) o).getPassword())
                && age == ((User) o).getAge()
                && stringEquals(firstName, ((User) o).getFirstName())
                && stringEquals(lastName, ((User) o).getLastName())
                // compare two set
                && roles.containsAll(((User) o).getRoles())
                && ((User) o).getRoles().containsAll(roles)
        );
    }

    private boolean stringEquals(String s1, String s2) {
        if (s1 == s2) {
            return true;
        }
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 != null && s1.equals(s2)) {
            return true;
        }
        return false;
    }
}

