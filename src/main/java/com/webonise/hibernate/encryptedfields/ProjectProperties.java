package com.webonise.hibernate.encryptedfields;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Cesar Valverde on 7/2/2015.
 */
public class ProjectProperties {

    private static Logger logger = Logger.getLogger(ProjectProperties.class.getName());
    private Properties properties = null;

    public static final String AES_KEY = "aes.key";

    public ProjectProperties(){
        properties = new Properties();
        try {
            properties.load(new FileInputStream("src/Project.properties"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Context: ProjectProperties", e);
        }
    }

    public String readProperty(String key){
        return (String) properties.get(key);
    }

}
