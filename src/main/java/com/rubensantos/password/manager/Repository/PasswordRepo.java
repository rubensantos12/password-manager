package com.rubensantos.password.manager.Repository;

import com.rubensantos.password.manager.Entity.Password;
import com.rubensantos.password.manager.Entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordRepo extends CrudRepository<Password, Integer> {

    Optional<Password> findByUserId(Integer id);
    Iterable<Password> findAllByUserId(Integer id);
}
