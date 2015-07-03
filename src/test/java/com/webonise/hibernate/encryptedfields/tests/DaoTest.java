package com.webonise.hibernate.encryptedfields.tests;

import com.webonise.hibernate.encryptedfields.*;
import org.junit.Assert;
import org.junit.BeforeClass;
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

    @BeforeClass
    public static void setUp(){
        projectProperties = new ProjectProperties();
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

        dao.commitTransaction();
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
        Connection con = null;
        try {
            con =  JDBC_Connection.getConnection();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Context: DaoTest.testInsertAndReadManually", e);
        }

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

        String myKey  = projectProperties.readProperty(ProjectProperties.AES_KEY);
        //Key is required
        Assert.assertNotNull(myKey);

        //Decrypt
        String decryptedColumn = null;

        SecretKeySpec secretKey = null;
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

        //secretKey is required
        Assert.assertNotNull(secretKey);

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            decryptedColumn = new String(cipher.doFinal(Base64.getDecoder().decode(column)));
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, "Context: Cipher_AES.decrypt", e);
        }

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
        Connection con = null;
        try {
            con =  JDBC_Connection.getConnection();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Context: DaoTest.testInsertAndReadManually2", e);
        }

        //Connection is required
        assertNotNull(con);

        String myKey  = projectProperties.readProperty(ProjectProperties.AES_KEY);
        //Key is required
        Assert.assertNotNull(myKey);

        //Encrypt
        String encryptedColumn = null;

        SecretKeySpec secretKey = null;
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

        //secretKey is required
        Assert.assertNotNull(secretKey);

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            encryptedColumn =  Base64.getEncoder().encodeToString(cipher.doFinal((testString).getBytes()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Context: Cipher_AES.encrypt", e);
        }

        //encryptedColumn is required
        Assert.assertNotNull(encryptedColumn);

        //Get column from the database directly
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
}