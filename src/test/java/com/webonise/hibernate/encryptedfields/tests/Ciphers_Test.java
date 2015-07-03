package com.webonise.hibernate.encryptedfields.tests;

import com.webonise.hibernate.encryptedfields.Cipher_AES;
import com.webonise.hibernate.encryptedfields.ProjectProperties;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Cesar Valverde on 6/30/2015.
 */
public class Ciphers_Test {

    private static Logger logger = Logger.getLogger(Ciphers_Test.class.getName());
    private static ProjectProperties projectProperties = null;

    @BeforeClass
    public static void setUp(){
        projectProperties = new ProjectProperties();
    }

    @Test
    public void testCipher_AES(){

        String key  = projectProperties.readProperty(ProjectProperties.AES_KEY);

        //Verify the key exist
        Assert.assertNotNull(key);

        String testString = "Test";
        String testExpectedString = "Test";

        //Init AES Cipher
        Cipher_AES cipher_aes = new Cipher_AES(key);

        //Encrypt
        byte[] encrypted = cipher_aes.encrypt(testString);
        logger.log(Level.INFO, "AES - Base64 encrypted: " + Base64.getEncoder().encodeToString(encrypted));

        //Decrypt
        String decrypted = cipher_aes.decrypt(encrypted);
        logger.log(Level.INFO, "AES - Decrypted: " + decrypted);

        //Compare
        Assert.assertEquals(testExpectedString, decrypted);
    }

}
