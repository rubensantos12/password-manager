package com.rubensantos.password.manager;

import com.rubensantos.password.manager.encryption.PasswordEncryption;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import javax.crypto.SecretKey;
import java.util.Base64;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
public class PasswordManagerApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(PasswordManagerApplication.class, args);
	}

}
