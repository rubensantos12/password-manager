package com.rubensantos.password.manager.controller;

import com.rubensantos.password.manager.encryption.PasswordEncryption;
import com.rubensantos.password.manager.entity.Password;
import com.rubensantos.password.manager.entity.User;
import com.rubensantos.password.manager.repository.PasswordRepo;
import com.rubensantos.password.manager.repository.UserRepo;
import com.rubensantos.password.manager.userstatus.CustomStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
public class PasswordController {

    private final PasswordRepo passwordRepo;

    private final UserRepo userRepo;

    PasswordEncryption passwordEncryption = new PasswordEncryption();

    public PasswordController(PasswordRepo passwordRepo, UserRepo userRepo) {
        this.passwordRepo = passwordRepo;
        this.userRepo = userRepo;
    }

    /**
     * Method used to retrieve a password from the database by ID
     *
     * @param id ID of the password saved
     * @return The password with said ID decrypted
     */

    @GetMapping("/getPassword/{id}")
    public ResponseEntity<Password> getPassword(@PathVariable(name = "id") Integer id, @RequestBody User currentUser) {

        //Retrieve user from database using the body received
        Optional<User> databaseUser = userRepo.findByUsername(currentUser.getUsername());

        if (databaseUser.isPresent()) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }

        String decryptedDatabasePassword =  passwordEncryption.decryptPassword(databaseUser.get().getPassword());

        //Check if user is logged in or not
        if (databaseUser.toString().equals("Optional.empty") ||
                !Objects.equals(decryptedDatabasePassword, currentUser.getPassword()) ||
                !databaseUser.get().isLoggedIn()) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        } else {

            //Retrieve the password from the database and assign it to a variable
            Optional<Password> encryptedPassword = passwordRepo.findById(id);

            if (encryptedPassword.toString().equals("Optional.empty")) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }

            if (encryptedPassword.get().getUserId() != databaseUser.get().getId()) {
                return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
            } else {

                //Create a new variable to later save the decrypted password
                Password decryptedPassword = new Password();

                //Assign all the required parameters to later return to the user
                decryptedPassword.setPassword(passwordEncryption.decryptPassword(encryptedPassword.get().getPassword()));
                decryptedPassword.setUsername(encryptedPassword.get().getUsername());
                decryptedPassword.setWebsite(encryptedPassword.get().getWebsite());
                decryptedPassword.setUrl(encryptedPassword.get().getUrl());

                //Returns the decrypted password to the user
                return new ResponseEntity<>(decryptedPassword, HttpStatus.OK);
            }
        }
    }

    /**
     * Method used to retrieve all passwords from the database
     *
     * @return A List of all usernames and passwords saved on the database
     */

    @GetMapping("/getAllPasswords")
    public ResponseEntity<List<Password>> getAllPasswords(@RequestBody User currentUser) {

        //Retrieve user from database using the body received
        Optional<User> databaseUser = userRepo.findByUsername(currentUser.getUsername());

        if (databaseUser.toString().equals("Optional.empty") || !databaseUser.get().getUsername().equals(currentUser.getUsername())) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }

        String decryptedPassword =  passwordEncryption.decryptPassword(databaseUser.get().getPassword());

        //Check if user is logged in or not
        if (databaseUser.toString().equals("Optional.empty") ||
                !Objects.equals(decryptedPassword, currentUser.getPassword()) ||
                !databaseUser.get().isLoggedIn()) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        } else {


            //Retrieve all the passwords from the database and save on an Iterable list
            Iterable<Password> listOfEncryptedPasswords = passwordRepo.findAllByUserId(databaseUser.get().getId());

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

            return new ResponseEntity<>(listOfDecryptedPasswords, HttpStatus.OK);
        }
    }

    /**
     * Method used to save a password to the database
     *
     * @param username Desired username used to save
     * @param password Desired password used to save
     */

    @PostMapping("/savePassword/{username}/{password}/{website}/{url}")
    public ResponseEntity<String> savePassword(@PathVariable(name = "username") String username,
                               @PathVariable(name = "password") String password,
                               @PathVariable(name = "website") String website,
                               @PathVariable(name = "url") String url,
                               @RequestBody User currentUser) {

        //Retrieve user from database using the body received
        Optional<User> databaseUser = userRepo.findByUsername(currentUser.getUsername());

        if (databaseUser.toString().equals("Optional.empty") || !databaseUser.get().getUsername().equals(currentUser.getUsername())) {
            return new ResponseEntity<>("User doesn't exists", HttpStatus.FORBIDDEN);
        }

        String decryptedPassword =  passwordEncryption.decryptPassword(databaseUser.get().getPassword());

        //Check if user is logged in or not
        if (databaseUser.toString().equals("Optional.empty") ||
                !Objects.equals(decryptedPassword, currentUser.getPassword()) ||
                !databaseUser.get().isLoggedIn()) {
            return new ResponseEntity<>("User is not logged in", HttpStatus.FORBIDDEN);
        } else {

            //Create a new Password and set all the required parameters to later save on the database
            Password passwordToSave = new Password();
            passwordToSave.setPassword(passwordEncryption.encryptPassword(password));
            passwordToSave.setUsername(username);
            passwordToSave.setWebsite(website);
            passwordToSave.setUrl(url);
            passwordToSave.setUserId(databaseUser.get().getId());

            //Save the password on the database
            passwordRepo.save(passwordToSave);

            return new ResponseEntity<>("Password saved successfully", HttpStatus.FORBIDDEN);

        }
    }

    @DeleteMapping("/deletePassword/{id}")
    public ResponseEntity<String> deletePassword(@PathVariable(name = "id") int id, @RequestBody User currentUser) {

        Optional<User> databaseUser = userRepo.findByUsername(currentUser.getUsername());

        if (databaseUser.toString().equals("Optional.empty") || !databaseUser.get().getUsername().equals(currentUser.getUsername())) {
            return new ResponseEntity<>("User doesn't exists", HttpStatus.FORBIDDEN);
        }

        String decryptedPassword = passwordEncryption.decryptPassword(databaseUser.get().getPassword());

        //Check if user is logged in or not
        if (databaseUser.toString().equals("Optional.empty") ||
                !Objects.equals(decryptedPassword, currentUser.getPassword()) ||
                !databaseUser.get().isLoggedIn()) {
            return new ResponseEntity<>("User is not logged in", HttpStatus.FORBIDDEN);
        } else {

            Optional<Password> passwordToDelete = passwordRepo.findById(id);

            if (passwordToDelete.toString().equals("Optional.empty") || !passwordToDelete.get().getUserId().equals(databaseUser.get().getId())) {
                return new ResponseEntity<>("Password with said ID doesn't exists", HttpStatus.BAD_REQUEST);
            } else {
                passwordRepo.deleteById(id);
                return new ResponseEntity<>("Password deleted successfully", HttpStatus.OK);
            }
        }
    }

    @PostMapping("/users/register")
    public ResponseEntity<String> registerUser(@RequestBody User newUser) {
        List<User> users = userRepo.findAll();

        for (User user : users) {
            if (user.getUsername().equals(newUser.getUsername())) {
                return new ResponseEntity<>("User already exists", HttpStatus.FORBIDDEN);
            }
        }
        String decryptedPassword = newUser.getPassword();

        newUser.setPassword(passwordEncryption.encryptPassword(decryptedPassword));
        userRepo.save(newUser);
        return new ResponseEntity<>("User registered", HttpStatus.OK);
    }

    @PostMapping("/users/login")
    public ResponseEntity<String> loginUser(@RequestBody User user) {
        List<User> users = userRepo.findAll();
        for (User other : users) {
            boolean passwordMatches = passwordEncryption.decryptPassword(other.getPassword()).equals(user.getPassword());
            System.out.println(passwordMatches);
            boolean usernameMatches = other.getUsername().equals(user.getUsername());
            if (passwordMatches && usernameMatches && other.isLoggedIn()) {
                return new ResponseEntity<>("User already logged in", HttpStatus.FORBIDDEN);
            } else if (passwordMatches && usernameMatches) {
                user.setId(other.getId());
                user.setLoggedIn(true);
                user.setPassword(passwordEncryption.encryptPassword(user.getPassword()));
                userRepo.save(user);
                return new ResponseEntity<>("User logged in", HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("User doesn't exists", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/users/logout")
    public ResponseEntity<String> logUserOut(@RequestBody User user) {
        List<User> users = userRepo.findAll();
        for (User other : users) {
            if (other.getUsername().equals(user.getUsername()) && other.isLoggedIn()) {
                user.setId(other.getId());
                user.setLoggedIn(false);
                user.setPassword(other.getPassword());
                userRepo.save(user);
                return new ResponseEntity<>("User logged out", HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("User is not logged in", HttpStatus.FORBIDDEN);
    }

    @DeleteMapping("/users/all")
    public CustomStatus deleteUsers() {
        userRepo.deleteAll();
        return CustomStatus.SUCCESS;
    }

}
