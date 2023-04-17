package com.rubensantos.password.manager.Repository;

import com.rubensantos.password.manager.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Integer> {
}
