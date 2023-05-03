package com.rubensantos.password.manager.Entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  long id;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private boolean loggedIn;

    public User() {
    }
    public User(@NotBlank String username,
                     @NotBlank String password) {
        this.username = username;
        this.password = password;
        this.loggedIn = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) &&
                Objects.equals(password, user.password);
    }    @Override
    public int hashCode() {
        return Objects.hash(id, username, password,
                loggedIn);
    }    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", loggedIn=" + loggedIn +
                '}';
    }

}
