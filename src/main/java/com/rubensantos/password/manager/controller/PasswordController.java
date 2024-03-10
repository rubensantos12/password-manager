package com.rubensantos.password.manager.controller;

import com.rubensantos.password.manager.config.AppConfiguration;
import com.rubensantos.password.manager.encryption.PasswordEncryption;
import com.rubensantos.password.manager.entity.Password;
import com.rubensantos.password.manager.repository.PasswordRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class PasswordController {

    private final PasswordRepo passwordRepo;
    private final PasswordEncryption passwordEncryption;

    private final SecretKey key;

    public PasswordController(PasswordRepo passwordRepo, AppConfiguration configuration, PasswordEncryption passwordEncryption) {
        this.passwordRepo = passwordRepo;
        this.passwordEncryption = passwordEncryption;
        key = configuration.getEncryptionKey();
    }

    /**
     * Method used to retrieve a password from the database by ID
     *
     * @param id ID of the password saved
     * @return The password with said ID decrypted
     */

    @GetMapping("/getPassword/{id}")
    public ResponseEntity<Password> getPassword(@PathVariable(name = "id") Integer id) throws Exception {

        Optional<Password> dbPassword = passwordRepo.findById(id);

        //Check if user is logged in or not
        if (dbPassword.isEmpty()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } else {


            //Create a new variable to later save the decrypted password
            Password decryptedPassword = new Password();

            try {
                //Decrypt password and set it on the new class to return to the caller
                decryptedPassword.setPassword(passwordEncryption.decrypt(dbPassword.get().getPassword(), key));
            } catch (Exception e) {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            decryptedPassword.setUsername(dbPassword.get().getUsername());
            decryptedPassword.setWebsite(dbPassword.get().getWebsite());
            decryptedPassword.setUrl(dbPassword.get().getUrl());

            //Returns the decrypted password to the user
            return new ResponseEntity<>(decryptedPassword, HttpStatus.OK);
        }
    }

    /**
     * Method used to retrieve all passwords from the database
     *
     * @return A List of all usernames and passwords saved on the database
     */
    @GetMapping("/getAllPasswords")
    public ResponseEntity<List<Password>> getAllPasswords() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {

        Iterable<Password> dbPasswords = passwordRepo.findAll();

        List<Password> allPasswords = new ArrayList<>();

        for (Password passwordToDecrypt : dbPasswords) {
            //Create a new password
            Password passwordToSave = new Password();

            //Try to decrypt password, throws exception if it fails
            try {
                passwordToSave.setPassword(passwordEncryption.decrypt(passwordToDecrypt.getPassword(), key));
            } catch (Exception e) {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);

            }

            //Set all the parameters from the Password retrieved from the database to the one shown to the user
            passwordToSave.setUsername(passwordToDecrypt.getUsername());
            passwordToSave.setId(passwordToDecrypt.getId());
            passwordToSave.setUrl(passwordToDecrypt.getUrl());
            passwordToSave.setWebsite(passwordToDecrypt.getWebsite());

            //Save the password to a list that is later returned to the user
            allPasswords.add(passwordToSave);
        }
        return new ResponseEntity<>(allPasswords, HttpStatus.OK);
    }

    /**
     * Method used to save a password to the database
     *
     * @param password Desired password used to save
     */
    @PostMapping("/savePassword")
    public ResponseEntity<String> savePassword(@RequestBody Password password) throws Exception {

        //Create a new Password and set all the required parameters to later save on the database
        Password passwordToSave = new Password();
        passwordToSave.setPassword(passwordEncryption.encrypt(password.getPassword(), key));
        passwordToSave.setUsername(password.getUsername());
        passwordToSave.setWebsite(password.getWebsite());
        passwordToSave.setUrl(password.getUrl());

        //Save the password on the database
        passwordRepo.save(passwordToSave);

        return new ResponseEntity<>("Password saved successfully", HttpStatus.FORBIDDEN);
    }


    @DeleteMapping("/deletePassword/{id}")
    public ResponseEntity<String> deletePassword(@PathVariable(name = "id") int id) throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {


        Optional<Password> passwordToDelete = passwordRepo.findById(id);

        if (passwordToDelete.isEmpty()) {
            return new ResponseEntity<>("Password with said ID doesn't exists", HttpStatus.BAD_REQUEST);
        } else {
            passwordRepo.deleteById(id);
            return new ResponseEntity<>("Password deleted successfully", HttpStatus.OK);
        }
    }
}