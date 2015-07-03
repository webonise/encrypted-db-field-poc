package com.webonise.hibernate.encryptedfields;

/**
 * Created by Cesar Valverde on 6/30/2015.
 */
public interface AnyCipher {
    Object decrypt(byte[] bytes);
    byte[] encrypt(Object object);
}
