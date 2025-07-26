package com.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity representing IdP email domain mappings.
 * 
 * This entity maps email domains to identity providers,
 * allowing automatic IdP selection based on user email.
 * 
 * @author SAML Spring Boot Application
 * @version 1.0.0
 */
@Entity
@Table(name = "idp_email_domains")
public class IdpEmailDomain {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idp_configuration_id", nullable = false)
    private IdpConfiguration idpConfiguration;
    
    @NotBlank(message = "Email domain is required")
    @Column(name = "email_domain", nullable = false, length = 100)
    private String emailDomain;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public IdpEmailDomain() {}
    
    public IdpEmailDomain(String emailDomain) {
        this.emailDomain = emailDomain;
    }
    
    public IdpEmailDomain(String emailDomain, Boolean isActive) {
        this.emailDomain = emailDomain;
        this.isActive = isActive;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public IdpConfiguration getIdpConfiguration() {
        return idpConfiguration;
    }
    
    public void setIdpConfiguration(IdpConfiguration idpConfiguration) {
        this.idpConfiguration = idpConfiguration;
    }
    
    public String getEmailDomain() {
        return emailDomain;
    }
    
    public void setEmailDomain(String emailDomain) {
        this.emailDomain = emailDomain;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "IdpEmailDomain{" +
                "id=" + id +
                ", emailDomain='" + emailDomain + '\'' +
                ", isActive=" + isActive +
                '}';
    }
} 