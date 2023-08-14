package com.itl.cloud.gateway;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionAES256 {

	@Value("${spring.SALTVALUE}")
	private String SALTVALUE;

	String userName = "infra";

	Cipher encryptionCipher;
	Cipher decryptionCipher;

	public String encrypt(String token) {
		try {
			byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(userName.toCharArray(), SALTVALUE.getBytes(), 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
			encryptionCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);

			return Base64.getEncoder().encodeToString(encryptionCipher.doFinal(token.getBytes(StandardCharsets.UTF_8)));

		} catch (Exception e) {
			System.out.println("Error occured during encryption: " + e.toString());

		}
		return null;
	}

	/* Decryption Method */
	public String decrypt(String token) {
		try {
			byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(userName.toCharArray(), SALTVALUE.getBytes(), 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
			// decryptionCipher = Cipher.getInstance("AES");
			decryptionCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			decryptionCipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
			return new String(decryptionCipher.doFinal(Base64.getDecoder().decode(token)));

		} catch (Exception e) {
			System.out.println("Error occured during decryption: " + e.toString());
		}
		return null;
	}

}
