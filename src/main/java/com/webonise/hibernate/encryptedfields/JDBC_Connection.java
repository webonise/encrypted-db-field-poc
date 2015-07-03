package com.webonise.hibernate.encryptedfields;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by Cesar Valverde on 7/1/2015.
 */
public class JDBC_Connection {

    public static Connection getConnection() throws SQLException {

        ProjectProperties projectProperties = new ProjectProperties();

        Properties connectionProps = new Properties();
        connectionProps.put("user", projectProperties.readProperty("mysql.user"));
        connectionProps.put("password", projectProperties.readProperty("mysql.password"));
        return DriverManager.getConnection(projectProperties.readProperty("mysql.url"), connectionProps);
    }
}