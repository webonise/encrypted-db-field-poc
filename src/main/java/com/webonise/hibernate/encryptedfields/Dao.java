package com.webonise.hibernate.encryptedfields;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;

/**
 * Created by Cesar Valverde on 6/30/2015.
 */
public class Dao {

    private Session session = null;

    public Long insertObject(Object obj) {
        return (Long) session.save(obj);
    }

    public void updateObject(Object obj) {
        session.update(obj);
    }

    public Object findObjectById(Class objectClass, Long id) {
        return session.get(objectClass, id);
    }

    public void deleteObject(Object obj) {
        session.delete(obj);
    }

    public Object findLastObject(String table, String field) {
        Query query = session.createQuery("from " + table + " order by " + field + " DESC");
        query.setMaxResults(1);
        return query.uniqueResult();
    }

    public void beginTransaction() {
        session = new Configuration().configure().buildSessionFactory().getCurrentSession();
        session.beginTransaction();
    }

    public void commitTransaction() {
        session.getTransaction().commit();
    }
}