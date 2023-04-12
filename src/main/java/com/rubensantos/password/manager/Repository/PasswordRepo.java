package com.rubensantos.password.manager.Repository;

import com.rubensantos.password.manager.Entity.Password;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordRepo extends CrudRepository<Password, Integer> {
}
