package com.example.erp.moraleducation.infrastructure.persistence;

import com.example.erp.platform.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "moral_activity")
public class MoralActivityEntity extends BaseEntity {

    @Column(name = "name", length = 128)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}