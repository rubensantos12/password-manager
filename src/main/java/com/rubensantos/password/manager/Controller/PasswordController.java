package com.rubensantos.password.manager.Controller;

import com.rubensantos.password.manager.Encryption.PasswordEncryption;
import com.rubensantos.password.manager.Entity.Password;
import com.rubensantos.password.manager.Entity.User;
import com.rubensantos.password.manager.Repository.PasswordRepo;
import com.rubensantos.password.manager.Repository.UserRepo;
import com.rubensantos.password.manager.UserStatus.CustomStatus;
import jakarta.transaction.Status;
import org.jasypt.util.password.PasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class PasswordController {

    @Autowired
    private PasswordRepo passwordRepo;

    @Autowired
    private UserRepo userRepo;

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    PasswordEncryption passwordEncryption = new PasswordEncryption();

    /**
     * Method used to retrieve a password from the database by ID
     *
     * @param id ID of the password saved
     * @return The password with said ID decrypted
     */

    @GetMapping("/getPassword/{id}")
    public Password getPassword(@PathVariable(name = "id") Integer id, @RequestBody @Valid User currentUser) {

        //Retrieve user from database using the body received
        Optional<User> loggedInUser = userRepo.findByUsername(currentUser.getUsername());

        if (!loggedInUser.get().isLoggedIn()) {
            return null;
        } else {

            //Retrieve the password from the database and assign it to a variable
            Optional<Password> encryptedPassword = passwordRepo.findById(id);

            if (encryptedPassword.get().getUserId() != loggedInUser.get().getId()) {
                return null;
            } else {

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
        }
    }

    /**
     * Method used to retrieve all passwords from the database
     *
     * @return A List of all usernames and passwords saved on the database
     */

    @GetMapping("/getAllPasswords")
    public List<Password> getAllPasswords(@RequestBody @Valid User currentUser) {

        //Retrieve user from database using the body received
        Optional<User> loggedInUser = userRepo.findByUsername(currentUser.getUsername());

        if (!loggedInUser.get().isLoggedIn()) {
            return null;
        } else {

            //Retrieve all the passwords from the database and save on an Iterable list
            Iterable<Password> listOfEncryptedPasswords = passwordRepo.findAllByUserId(loggedInUser.get().getId());

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
                               @PathVariable(name = "url") String url,
                               @Valid @RequestBody User currentUser) {

        //Retrieve user from database using the body received
        Optional<User> loggedInUser = userRepo.findByUsername(currentUser.getUsername());

        //Check if user is logged in or not
        if (!loggedInUser.get().isLoggedIn()) {
            return null;
        } else {

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
    }

    @PostMapping("/users/register")
    public CustomStatus registerUser(@Valid @RequestBody User newUser) {
        List<User> users = userRepo.findAll();

        for (User user : users) {
            if (user.equals(newUser)) {
                System.out.println("User Already exists!");
                return CustomStatus.USER_ALREADY_EXISTS;
            }
        }
        String decryptedPassword = newUser.getPassword();

        newUser.setPassword(bCryptPasswordEncoder.encode(decryptedPassword));
        userRepo.save(newUser);
        return CustomStatus.SUCCESS;
    }

    @PostMapping("/users/login")
    public CustomStatus loginUser(@Valid @RequestBody User user) {
        List<User> users = userRepo.findAll();
        for (User other : users) {
            boolean passwordMatches = bCryptPasswordEncoder.matches(user.getPassword(), other.getPassword());
            System.out.println(passwordMatches);
            boolean usernameMatches = other.getUsername().equals(user.getUsername());
            if (passwordMatches && usernameMatches && other.isLoggedIn()) {
                return CustomStatus.USER_ALREADY_LOGGED_IN;
            } else if (passwordMatches && usernameMatches) {
                user.setId(other.getId());
                user.setLoggedIn(true);
                user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
                userRepo.save(user);
                return CustomStatus.SUCCESS;
            }
        }
        return CustomStatus.FAILURE;
    }

    @PostMapping("/users/logout")
    public CustomStatus logUserOut(@Valid @RequestBody User user) {
        List<User> users = userRepo.findAll();
        for (User other : users) {
            if (other.getUsername().equals(user.getUsername()) && other.isLoggedIn()) {
                user.setId(other.getId());
                user.setLoggedIn(false);
                user.setPassword(other.getPassword());
                userRepo.save(user);
                return CustomStatus.SUCCESS;
            }
        }
        return CustomStatus.FAILURE;
    }

    @DeleteMapping("/users/all")
    public CustomStatus deleteUsers() {
        userRepo.deleteAll();
        return CustomStatus.SUCCESS;
    }

}
