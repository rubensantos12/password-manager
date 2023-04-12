package com.rubensantos.password.manager.Controller;

import com.rubensantos.password.manager.Entity.Password;
import com.rubensantos.password.manager.Repository.PasswordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PasswordController {

    @Autowired
    private PasswordRepo passwordRepo;

    @GetMapping("/getPassword/{id}")
    public Object getPassword(@PathVariable(name="id") Integer id) {
        return passwordRepo.findById(id);
    }

    @PostMapping("/savePassword/{username}/{password}")
    public void savePassword(@PathVariable(name="username") String username, @PathVariable(name="password") String password) {

    }

}
