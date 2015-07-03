package com.webonise.hibernate.encryptedfields.tests;

import com.webonise.hibernate.encryptedfields.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Created by Cesar Valverde on 6/30/2015.
 */
public class DaoTest {

    private static Logger logger = Logger.getLogger(DaoTest.class.getName());

    private static ProjectProperties projectProperties = null;
    private static SecretKeySpec secretKey = null;

    @BeforeClass
    public static void setUp(){

        projectProperties = new ProjectProperties();

        //Set up key
        String myKey  = projectProperties.readProperty(ProjectProperties.AES_KEY);

        byte[] key = null;
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // use only first 128 bit
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Context: Cipher_AES.setKey", e);
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, "Context: Cipher_AES.setKey", e);
        }

    }

    /**
     * Using Hibernate only
     */
    @Test
     public void testInsertAndRead() {

        String testString = "Test String";

        // Insert Entity using Hibernate
        TestEntity testEntity1 = new TestEntity();
        testEntity1.setField(testString);
        assertNull(testEntity1.getId());

        Dao dao = new Dao();
        dao.beginTransaction();
        dao.insertObject(testEntity1);
        assertNotNull(testEntity1.getId());

        dao.commitTransaction();

        // Read and compare using Hibernate
        dao = new Dao();
        dao.beginTransaction();

        TestEntity testEntity2 = (TestEntity) dao.findObjectById(TestEntity.class, testEntity1.getId());

        //Compare
        assertEquals(testEntity2.getField(), testEntity1.getField());

    }

    /**
        Save entity with hibernate
        and get the column with JDBC
     */
    @Test
    public void testInsertAndReadManually() throws SQLException {

        String testString = "Test String";

        // Insert Entity
        TestEntity testEntity = new TestEntity();
        testEntity.setField(testString);
        assertNull(testEntity.getId());

        Dao dao = new Dao();
        dao.beginTransaction();
        dao.insertObject(testEntity);
        assertNotNull(testEntity.getId());

        dao.commitTransaction();

        /*
            Read and compare using JDBC
         */

        //Get a connection to the database
        Connection con = getConnection();

        //Connection is required
        assertNotNull(con);

        //Get column from the database directly
        Statement stmt = null;
        String query = "select * from " + TestEntity.TABLE + " where id=" + testEntity.getId();
        String column = null;
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                column = rs.getString(TestEntity.FIELD);
            }
        } catch (SQLException e ) {
            logger.log(Level.SEVERE, "Context: DaoTest.testInsertAndReadManually", e);
        } finally {
            if (stmt != null) { stmt.close(); }
        }

        //Column is required
        assertNotNull(column);

        //secretKey is required
        Assert.assertNotNull(secretKey);

        //Decrypt
        String decryptedColumn = manualCipherDecrypt(base64Decode(column));

        //decryptedColumn is required
        Assert.assertNotNull(decryptedColumn);

        //Compare
        assertEquals(decryptedColumn, testEntity.getField());

    }

    /**
        Save entity with JDBC
        and get the column with Hibernate
     */
    @Test
    public void testInsertAndReadManually2() throws SQLException {

        String testString = "Test String";

        // Insert Entity

        //Get a connection to the database
        Connection con = getConnection();

        //Connection is required
        assertNotNull(con);

        //secretKey is required
        Assert.assertNotNull(secretKey);

        String encryptedColumn = base64Encode(manualCipherEncrypt(testString));
        //encryptedColumn is required

        Assert.assertNotNull(encryptedColumn);

        //Insert column to the database directly
        Statement stmt = null;
        String sql = "INSERT INTO " + TestEntity.TABLE + " (" + TestEntity.FIELD + ") VALUES ('" + encryptedColumn + "')";
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e ) {
            logger.log(Level.SEVERE, "Context: DaoTest.testInsertAndReadManually2", e);
        } finally {
            if (stmt != null) { stmt.close(); }
        }

        //Get last added entity
        Dao dao = new Dao();
        dao.beginTransaction();
        TestEntity testEntity = (TestEntity) dao.findLastObject(TestEntity.TABLE, TestEntity.FIELD);

        //testEntity is required
        Assert.assertNotNull(testEntity);

        //Compare
        assertEquals(testString, testEntity.getField());

    }

    /**
     * Test of the formula to calculate the size of the cipher text in the database
     */
    @Test
    public void definedSizeTest(){

        String testString = "00000000000000000000000000000000";

        System.out.println("Text size: " + testString.length());

        //secretKey is required
        Assert.assertNotNull(secretKey);

        /*
            Using AES algorithm
         */
        byte[] encryptedText = manualCipherEncrypt(testString);
        int encryptedTextSize = encryptedText.length;
        System.out.println("Encrypted Text size: " + encryptedTextSize);

        String encodedText = base64Encode(encryptedText);
        int encodedTextSize = encodedText.length();
        System.out.println("Encoded Text size: " + encodedTextSize);

        /*
            Using Formula
         */
        int calcEncryptedTextSize = calcSizeCipherText(testString.length());
        System.out.println("Calculated Encrypted Text size: " + calcEncryptedTextSize);

        int calcEncodedTextSize = calcSizeCipherTextBase64Encoded(encryptedText.length);
        System.out.println("Calculated Cipher Base64 Encoded Text size: " + calcEncodedTextSize);

        //Must be the same
        Assert.assertEquals(encryptedTextSize, calcEncryptedTextSize);

        //Must be the same
        Assert.assertEquals(encodedTextSize, calcEncodedTextSize);

    }

    public int calcSizeCipherText(int textSize){
        return textSize + 16 - (textSize%16);
    }

    public int calcSizeCipherTextBase64Encoded(int numBytes){
        return (numBytes + 2 - ((numBytes + 2) % 3)) / 3 * 4;
    }

    /**
     * @param text to decode
     * @return decoded bytes
     */
    public byte[] base64Decode(String text){
        return Base64.getDecoder().decode(text);
    }

    /**
     * @param text to encode
     * @return Base64 encoded String
     */
    public String base64Encode(byte[] text){
        return Base64.getEncoder().encodeToString(text);
    }

    /**
     * @param textBase64Encoded
     * @return the decrypted text
     */
    public String manualCipherDecrypt(byte[] textBase64Encoded){
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(textBase64Encoded));
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, "Context: Cipher_AES.decrypt", e);
        }
        return null;
    }

    /**
     * @param text to encrypt
     * @return Encrypted text
     */
    public byte[] manualCipherEncrypt(String text){
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal((text).getBytes());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Context: Cipher_AES.encrypt", e);
        }
        return null;
    }

    /**
     * @return JDBC database connection
     */
    public Connection getConnection(){
        Connection connection = null;
        try {
            connection =  JDBC_Connection.getConnection();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Context: DaoTest.getConnection", e);
        }
        return connection;
    }

}