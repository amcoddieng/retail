package com.example.retail.domain;

import javax.persistence.*;

@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String code;

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String c) {
        code = c;
    }
}