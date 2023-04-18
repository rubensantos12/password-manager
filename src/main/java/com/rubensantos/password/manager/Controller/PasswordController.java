package com.rubensantos.password.manager.Controller;

import com.rubensantos.password.manager.Dto.LoginDto;
import com.rubensantos.password.manager.Dto.SignUpDto;
import com.rubensantos.password.manager.Encryption.PasswordEncryption;
import com.rubensantos.password.manager.Entity.Password;
import com.rubensantos.password.manager.Entity.Role;
import com.rubensantos.password.manager.Entity.User;
import com.rubensantos.password.manager.Repository.PasswordRepo;
import com.rubensantos.password.manager.Repository.RoleRepo;
import com.rubensantos.password.manager.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
public class PasswordController {

    @Autowired
    private PasswordRepo passwordRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

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


    @PostMapping("/signin")
    public ResponseEntity<String> authenticateUser(@RequestBody LoginDto loginDto){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsernameOrEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new ResponseEntity<>("User signed-in successfully!.", HttpStatus.OK);
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpDto signUpDto){

        // checks if username exists on the database
        if(userRepo.existsByUsername(signUpDto.getUsername())){
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        // checks if email exists on the database
        if(userRepo.existsByEmail(signUpDto.getEmail())){
            return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
        }

        // create user object
        User user = new User();
        user.setName(signUpDto.getName());
        user.setUsername(signUpDto.getUsername());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));

        Role roles = roleRepository.findByName("ROLE_ADMIN").get();
        user.setRoles(Collections.singleton(roles));

        userRepo.save(user);

        System.out.println(user.getEmail());

        return new ResponseEntity<>("User registered successfully", HttpStatus.OK);

    }

}
