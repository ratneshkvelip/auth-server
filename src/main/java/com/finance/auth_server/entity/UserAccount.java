package com.finance.auth_server.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="user_account")
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id",nullable=false,unique=true) private Long userId;
    @Column(precision=19,scale=4,nullable=false) private BigDecimal balance=BigDecimal.ZERO;
    @Column(precision=10,scale=2) private BigDecimal credit=BigDecimal.ZERO;
    @Column(precision=10,scale=2) private BigDecimal debit=BigDecimal.ZERO;
    @Column(name="created_at") private Instant createdAt;
    @Column(name="updated_at") private Instant updatedAt;

    public UserAccount(Long userId, BigDecimal balance, BigDecimal credit, BigDecimal debit) {
        this.userId = userId;
        this.balance = balance;
        this.credit = credit;
        this.debit = debit;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
