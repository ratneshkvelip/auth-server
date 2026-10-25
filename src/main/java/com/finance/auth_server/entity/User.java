package com.finance.auth_server.entity;


import com.finance.auth_server.enums.UserStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users", schema = "finance",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_username", columnNames = "user_name"),
                @UniqueConstraint(name = "uk_users_mobile", columnNames = "mobile_number")
        })
public class User {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false, length = 100)
    private String username;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected User() {}

    public User(String username, String email, String name, String mobileNumber, String passwordHash) {
        this.username = username;
        this.email = email;
        this.name=name;
        this.mobileNumber = mobileNumber;
        this.passwordHash = passwordHash;
        this.status = UserStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getMobileNumber() { return mobileNumber; }
    public String getPasswordHash() { return passwordHash; }
    public UserStatus getStatus() { return status; }}
