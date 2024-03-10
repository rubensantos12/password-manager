package com.rubensantos.password.manager.repository;

import com.rubensantos.password.manager.entity.Password;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordRepo extends CrudRepository<Password, Integer> {

}
