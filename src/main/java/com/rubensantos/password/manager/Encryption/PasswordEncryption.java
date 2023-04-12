package com.rubensantos.password.manager.Encryption;

import org.jasypt.util.text.BasicTextEncryptor;

public class PasswordEncryption {

    Details dt = new Details();
    BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();

    public PasswordEncryption() {
        setupDataEncryptor();
    }

    public void setupDataEncryptor() {
        basicTextEncryptor.setPassword(dt.getKey());
    }

    public String encryptPassword(String password) {
        return basicTextEncryptor.encrypt(password);
    }

    public String decryptPassword(String encryptedPassword) {
        return basicTextEncryptor.decrypt(encryptedPassword);
    }
}
