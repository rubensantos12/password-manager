package com.rubensantos.password.manager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@ConfigurationProperties(prefix = "passwordmanager")
@Getter
@Setter
public class AppConfiguration {

    private String key;

    public void setEncryptionKeyBase64(String encryptionKeyBase64) {
        this.key = encryptionKeyBase64;
    }

    // Method to decode Base64-encoded key and reconstruct SecretKey
    public SecretKey getEncryptionKey() {
        byte[] decodedKeyBytes = Base64.getDecoder().decode(key);
        return new SecretKeySpec(decodedKeyBytes, "AES");
    }
}
