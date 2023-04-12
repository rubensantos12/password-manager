package com.rubensantos.password.manager.Controller;

import com.rubensantos.password.manager.Encryption.PasswordEncryption;
import com.rubensantos.password.manager.Entity.Password;
import com.rubensantos.password.manager.Repository.PasswordRepo;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class PasswordController {

    @Autowired
    private PasswordRepo passwordRepo;

    PasswordEncryption passwordEncryption = new PasswordEncryption();

    @GetMapping("/getPassword/{id}")
    public Object getPassword(@PathVariable(name="id") Integer id) {
        Optional<Password> encryptedPassword = passwordRepo.findById(id);
        Password decryptedPassword = new Password();
        decryptedPassword.setPassword(passwordEncryption.decryptPassword(encryptedPassword.get().getPassword()));
        decryptedPassword.setUsername(encryptedPassword.get().getUsername());
        return decryptedPassword;
    }

    @PostMapping("/savePassword/{username}/{password}")
    public void savePassword(@PathVariable(name="username") String username, @PathVariable(name="password") String password) {
        Password passwordToSave = new Password();
        passwordToSave.setPassword(passwordEncryption.encryptPassword(password));
        passwordToSave.setUsername(username);
        passwordRepo.save(passwordToSave);
    }

}
