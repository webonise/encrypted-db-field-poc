package com.webonise.hibernate.encryptedfields;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


/**
 * From: http://aesencryption.net/
 */
public class Cipher_AES implements AnyCipher{

    private static Logger logger = Logger.getLogger(Cipher_AES.class.getName());
    private static SecretKeySpec secretKey = null;
    private static byte[] key = null;

    private static String AES_PADDING = "AES/ECB/PKCS5Padding";
    private static String AlGORITHM = "AES";

    public Cipher_AES(String key){
        setKey(key);
    }
    
    public static void setKey(String myKey){
    	MessageDigest sha = null;
		try {
			key = myKey.getBytes(StandardCharsets.UTF_8);
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
	    	key = Arrays.copyOf(key, 16); // use only first 128 bit
		    secretKey = new SecretKeySpec(key, AlGORITHM);
		} catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Context: Cipher_AES.setKey", e);
		}
    }

    @Override
    public String decrypt(byte[] bytes) {
        logger.log(Level.INFO, "Cipher_AES.decrypting...");
        try {
            Cipher cipher = Cipher.getInstance(AES_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(bytes)));
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, "Context: Cipher_AES.decrypt", e);
        }
        return null;
    }

    @Override
    public byte[] encrypt(Object object) {
        logger.log(Level.INFO, "Cipher_AES.encrypting...");
        try {
            Cipher cipher = Cipher.getInstance(AES_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] cipherText = cipher.doFinal(((String) object).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encode(cipherText);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Context: Cipher_AES.encrypt", e);
        }
        return null;
    }

}