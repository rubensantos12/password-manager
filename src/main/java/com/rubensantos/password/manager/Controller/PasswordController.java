package com.rubensantos.password.manager.Controller;

import com.rubensantos.password.manager.Encryption.PasswordEncryption;
import com.rubensantos.password.manager.Entity.Password;
import com.rubensantos.password.manager.Entity.User;
import com.rubensantos.password.manager.Repository.PasswordRepo;
import com.rubensantos.password.manager.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class PasswordController {

    @Autowired
    private PasswordRepo passwordRepo;

    @Autowired
    private UserRepo userRepo;


    PasswordEncryption passwordEncryption = new PasswordEncryption();

    /**
     * Method used to retrieve a password from the database by ID
     *
     * @param id ID of the password saved
     * @return The password with said ID decrypted
     */

    @GetMapping("/getPassword/{id}")
    public Password getPassword(@PathVariable(name = "id") Integer id) {

        //Retrieve the password from the database and assign it to a variable
        Optional<Password> encryptedPassword = passwordRepo.findById(id);

        //Create a new variable to later save the decrypted password
        Password decryptedPassword = new Password();

        //Assign all the required parameters to later return to the user
        decryptedPassword.setPassword(passwordEncryption.decryptPassword(encryptedPassword.get().getPassword()));
        decryptedPassword.setUsername(encryptedPassword.get().getUsername());
        decryptedPassword.setWebsite(encryptedPassword.get().getWebsite());
        decryptedPassword.setUrl(encryptedPassword.get().getUrl());

        //Returns the decrypted password to the user
        return decryptedPassword;
    }

    /**
     * Method used to retrieve all passwords from the database
     *
     * @return A List of all usernames and passwords saved on the database
     */

    @GetMapping("/getAllPasswords")
    public List<Password> getAllPasswords() {

        //Retrieve all the passwords from the database and save on an Iterable list
        Iterable<Password> listOfEncryptedPasswords = passwordRepo.findAll();

        //Create a new List to save all the passwords after they are decrypted
        List<Password> listOfDecryptedPasswords = new ArrayList<>();

        for (Password passwordToDecrypt : listOfEncryptedPasswords) {
            //Create a new password
            Password passwordToSave = new Password();

            //Set all the parameters from the Password retrieved from the database to the one shown to the user
            passwordToSave.setPassword(passwordEncryption.decryptPassword(passwordToDecrypt.getPassword()));
            passwordToSave.setUsername(passwordToDecrypt.getUsername());
            passwordToSave.setId(passwordToDecrypt.getId());
            passwordToSave.setUrl(passwordToDecrypt.getUrl());
            passwordToSave.setWebsite(passwordToDecrypt.getWebsite());

            //Save the password to a list that is later returned to the user
            listOfDecryptedPasswords.add(passwordToSave);
        }

        return listOfDecryptedPasswords;
    }

    /**
     * Method used to save a password to the database
     *
     * @param username Desired username used to save
     * @param password Desired password used to save
     */

    @PostMapping("/savePassword/{username}/{password}/{website}/{url}")
    public String savePassword(@PathVariable(name = "username") String username,
                               @PathVariable(name = "password") String password,
                               @PathVariable(name = "website") String website,
                               @PathVariable(name = "url") String url) {
        //Create a new Password and set all the required parameters to later save on the database
        Password passwordToSave = new Password();
        passwordToSave.setPassword(passwordEncryption.encryptPassword(password));
        passwordToSave.setUsername(username);
        passwordToSave.setWebsite(website);
        passwordToSave.setUrl(url);

        //Save the password on the database
        passwordRepo.save(passwordToSave);

        return "Password successfully saved!";
    }

    /**
     * Method used to retrieve a password encrypted from the database
     *
     * @param id The ID of the Password saved
     * @return The Password completely encrypted
     */

    @GetMapping("/getEncryptedPassword/{id}")
    public Object getEncryptedPassword(@PathVariable(name = "id") Integer id) {
        return passwordRepo.findById(id);
    }

    /**
     * Method used to log in the user into the application
     *
     * @param username Username used on register
     * @param password Password used on register
     * @return The user back
     */
    @GetMapping("/userLogin/{username}/{password}")
    public User userLogIn(@PathVariable(name = "username") String username, @PathVariable(name = "password") String password) {
        return null;
    }

    /**
     * Method used to register the user and save it on the database
     *
     * @param username Desired username to register
     * @param password Desired password to register
     * @param email Desired email to register
     * @return The registered user back
     */
    @PostMapping("/userRegistration/{username}/{password}/{email}")
    public User userRegistration(@PathVariable(name = "username") String username, @PathVariable(name = "password") String password, @PathVariable(name = "email") String email) {
        User userToSave = new User();
        userToSave.setEmail(email);
        userToSave.setPassword(passwordEncryption.encryptPassword(password));
        userToSave.setUsername(username);

        userRepo.save(userToSave);

        return userToSave;
    }

}
