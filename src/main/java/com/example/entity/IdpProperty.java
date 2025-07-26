package com.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity representing IdP configuration properties.
 * 
 * This entity stores key-value pairs for SAML configuration
 * properties associated with identity providers.
 * 
 * @author SAML Spring Boot Application
 * @version 1.0.0
 */
@Entity
@Table(name = "idp_properties")
public class IdpProperty {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idp_configuration_id", nullable = false)
    private IdpConfiguration idpConfiguration;
    
    @NotBlank(message = "Property name is required")
    @Column(name = "property_name", nullable = false, length = 100)
    private String propertyName;
    
    @Column(name = "property_value", columnDefinition = "TEXT")
    private String propertyValue;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public IdpProperty() {}
    
    public IdpProperty(String propertyName, String propertyValue) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
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
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    
    public String getPropertyValue() {
        return propertyValue;
    }
    
    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
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
        return "IdpProperty{" +
                "id=" + id +
                ", propertyName='" + propertyName + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                '}';
    }
} 