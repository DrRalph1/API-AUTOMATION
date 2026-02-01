package com.usg.apiAutomation.entities;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "tb_user_otps", indexes = {
        @Index(name = "idx_user_otps_user_id", columnList = "user_id")
})
public class UserOtpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "otp_code", nullable = false)
    private String otpCode;

    @Column(name = "expiration_time", nullable = false)
    private Instant expirationTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    public UserOtpEntity() {}

    public UserOtpEntity(String userId, String otpCode, Instant expirationTime) {
        this.userId = userId;
        this.otpCode = otpCode;
        this.expirationTime = expirationTime;
        this.verified = false;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    // Getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public Instant getExpirationTime() { return expirationTime; }
    public void setExpirationTime(Instant expirationTime) { this.expirationTime = expirationTime; }

    public Instant getCreatedAt() { return createdAt; }
    // No setter for createdAt since it is set automatically on persist

    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
}
