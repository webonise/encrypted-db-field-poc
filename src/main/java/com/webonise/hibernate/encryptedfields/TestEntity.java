package com.webonise.hibernate.encryptedfields;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;

@Table( name = "TestEntity" )
@Entity
@TypeDefs(value = { @TypeDef(name = "EncryptedField", typeClass = EncryptedField.class) })
/**
 * Created by Cesar Valverde on 6/30/2015.
 */
public class TestEntity {

    public static final String TABLE = "TestEntity";
    public static final String FIELD = "field";

    private Long id;
    private String field;
    public static final String DEFAULT_VALUE = "DEFAULT";

    public TestEntity() {
       this.field = TestEntity.DEFAULT_VALUE;
    }

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Type(type="EncryptedField")
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}