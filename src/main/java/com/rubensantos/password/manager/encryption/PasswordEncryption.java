package com.rubensantos.password.manager.encryption;

import org.jasypt.util.text.BasicTextEncryptor;

public class PasswordEncryption {

    // Details class has the key used by the encryption class
    Details dt = new Details();
    BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();

    /**
     * Class constructor
     */

    public PasswordEncryption() {
        setupDataEncryptor();
    }

    /**
     * Basic method to start the text encryptor with the key introduced in the Details class
     * @see Details
     */

    public void setupDataEncryptor() {
        basicTextEncryptor.setPassword(dt.getKey());
    }

    /**
     *
     * @param password The password extracted from the Password object
     * @see com.rubensantos.password.manager.entity.Password
     * @return The password encrypted
     */

    public String encryptPassword(String password) {
        return basicTextEncryptor.encrypt(password);
    }

    /**
     *
     * @param encryptedPassword The password extracted from the Password object
     * @see com.rubensantos.password.manager.entity.Password
     * @return The password decrypted
     */

    public String decryptPassword(String encryptedPassword) {
        return basicTextEncryptor.decrypt(encryptedPassword);
    }
}
