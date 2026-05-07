package com.tobi.qbank.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean emailVerified = false;


    @Column(nullable = false)
    private String role = "ROLE_USER";

    public User() {}

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getFullName()                { return fullName; }
    public void setFullName(String fullName)   { this.fullName = fullName; }

    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }

    public String getPassword()                { return password; }
    public void setPassword(String password)   { this.password = password; }

    public boolean isEmailVerified()                       { return emailVerified; }
    public void setEmailVerified(boolean emailVerified)    { this.emailVerified = emailVerified; }

    public String getRole()                    { return role; }
    public void setRole(String role)           { this.role = role; }
}