package com.rubensantos.password.manager.Controller;

import com.rubensantos.password.manager.Encryption.PasswordEncryption;
import com.rubensantos.password.manager.Entity.Password;
import com.rubensantos.password.manager.Repository.PasswordRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class PasswordController {

    @Autowired
    private PasswordRepo passwordRepo;

    PasswordEncryption passwordEncryption = new PasswordEncryption();

    /**
     *Method used to retrieve a password from the database by ID
     *
     * @param id ID of the password saved
     * @return The password with said ID decrypted
     */

    @GetMapping("/getPassword/{id}")
    public Object getPassword(@PathVariable(name="id") Integer id) {
        Optional<Password> encryptedPassword = passwordRepo.findById(id);
        Password decryptedPassword = new Password();
        decryptedPassword.setPassword(passwordEncryption.decryptPassword(encryptedPassword.get().getPassword()));
        decryptedPassword.setUsername(encryptedPassword.get().getUsername());
        return decryptedPassword;
    }

    /**
     * Method used to retrieve all passwords from the database
     * @return A List of all usernames and passwords saved on the database
     */

    @GetMapping("/getAllPasswords")
    public List<Password> getAllPasswords() {
        Iterable<Password> listOfEncryptedPasswords = passwordRepo.findAll();
        List<Password> listOfDecryptedPasswords = new ArrayList<>();

        for (Password passwordToDecrypt : listOfEncryptedPasswords) {
            System.out.println(passwordToDecrypt.getPassword());
            Password passwordToSave = new Password();
            passwordToSave.setPassword(passwordEncryption.decryptPassword(passwordToDecrypt.getPassword()));
            passwordToSave.setUsername(passwordToDecrypt.getUsername());
            passwordToSave.setId(passwordToDecrypt.getId());
            listOfDecryptedPasswords.add(passwordToSave);
        }

        return listOfDecryptedPasswords;
    }

    /**
     * Method used to save a password to the database
     * @param username Desired username used to save
     * @param password Desired password used to save
     */

    @PostMapping("/savePassword/{username}/{password}")
    public String savePassword(@PathVariable(name="username") String username, @PathVariable(name="password") String password) {
        Password passwordToSave = new Password();
        passwordToSave.setPassword(passwordEncryption.encryptPassword(password));
        passwordToSave.setUsername(username);
        passwordRepo.save(passwordToSave);
        return "Password successfully saved!";
    }

    /**
     *Method used to retrieve a password encrypted from the database
     *
     * @param id The ID of the Password saved
     * @return The Password completely encrypted
     */

    @GetMapping("/getEncryptedPassword/{id}")
    public Object getEncryptedPassword(@PathVariable(name="id") Integer id) {
        return passwordRepo.findById(id);
    }

}
