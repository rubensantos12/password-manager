package com.rubensantos.password.manager.encryption;


import com.rubensantos.password.manager.entity.Password;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.logging.Logger;

public class PasswordEncryption {

    // Details class has the key used by the encryption class

    Logger logger = Logger.getLogger(getClass().getName());
    private byte[] secretKey = new byte[0];
    private byte[] salt = new byte[0];
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int GCM_TAG_LENGTH = 128;
    /**
     * Class constructor
     */

    public PasswordEncryption() {
        setupDataEncryptor();
    }

    public void setupDataEncryptor() {
        try {
            // Generate a secure random salt
            salt = generateSalt();
            // Generate a secure random secret key
            secretKey = generateSecretKey();
        } catch (NoSuchAlgorithmException e) {
            logger.info(e.toString());
        }
    }

    private static byte[] generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[16]; // Adjust the size based on your requirements
        secureRandom.nextBytes(salt);
        return salt;
    }

    private static byte[] generateSecretKey() throws NoSuchAlgorithmException {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32]; // Adjust the size based on your requirements
        secureRandom.nextBytes(randomBytes);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(randomBytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexStringBuilder = new StringBuilder();
        for (byte aByte : bytes) {
            hexStringBuilder.append(String.format("%02x", aByte));
        }
        return hexStringBuilder.toString();
    }

    /**
     *
     * @param password The password extracted from the Password object
     * @see Password
     * @return The password encrypted
     */

    public String encryptPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(bytesToHex(secretKey).toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[GCM_TAG_LENGTH / 8];
        cipher.init(Cipher.ENCRYPT_MODE, secret, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] encryptedData = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encryptedData);
    }

    public String decryptPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        String[] parts = password.split(":");
        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] encryptedBytes = Base64.getDecoder().decode(parts[1]);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(new String(secretKey).toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}