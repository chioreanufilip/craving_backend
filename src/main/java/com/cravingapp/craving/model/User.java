package com.cravingapp.craving.model;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.Data;

@Data
@Table(name = "user")
public class User {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column
    private String username;
    @Column
    private String password_hash;
    @Column
    private String email;
    @Column
    private String bio;
    @Column
    private String profile_picture_url;
    @Timestamp
    private Timestamp created_at;
}
