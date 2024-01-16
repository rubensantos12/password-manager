package com.rubensantos.password.manager.repository;

import com.rubensantos.password.manager.entity.Password;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordRepo extends CrudRepository<Password, Integer> {

    Iterable<Password> findAllByUserId(Long id);

}
