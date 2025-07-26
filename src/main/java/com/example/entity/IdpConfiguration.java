package com.example.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity representing Identity Provider (IdP) configuration.
 * 
 * This entity stores SAML IdP metadata and configuration settings
 * for different identity providers like Azure AD.
 * 
 * @author SAML Spring Boot Application
 * @version 1.0.0
 */
@Entity
@Table(name = "idp_configurations")
public class IdpConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "IdP ID is required")
    @Column(name = "idp_id", unique = true, nullable = false, length = 100)
    private String idpId;
    
    @NotBlank(message = "IdP name is required")
    @Column(name = "idp_name", nullable = false, length = 255)
    private String idpName;
    
    @NotBlank(message = "IdP entity ID is required")
    @Column(name = "idp_entity_id", nullable = false, length = 255)
    private String idpEntityId;
    
    @NotBlank(message = "IdP SSO URL is required")
    @Column(name = "idp_sso_url", nullable = false, length = 500)
    private String idpSsoUrl;
    
    @Column(name = "idp_slo_url", length = 500)
    private String idpSloUrl;
    
    @Column(name = "idp_certificate", columnDefinition = "TEXT")
    private String idpCertificate;
    
    @Column(name = "logo_url", length = 500)
    private String logoUrl;
    
    @Column(name = "display_name", length = 200)
    private String displayName;
    
    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @NotNull
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Relationships
    @OneToMany(mappedBy = "idpConfiguration", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IdpProperty> properties = new ArrayList<>();
    
    @OneToMany(mappedBy = "idpConfiguration", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IdpEmailDomain> emailDomains = new ArrayList<>();
    
    // Constructors
    public IdpConfiguration() {}
    
    public IdpConfiguration(String idpId, String idpName, String idpEntityId, String idpSsoUrl) {
        this.idpId = idpId;
        this.idpName = idpName;
        this.idpEntityId = idpEntityId;
        this.idpSsoUrl = idpSsoUrl;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getIdpId() {
        return idpId;
    }
    
    public void setIdpId(String idpId) {
        this.idpId = idpId;
    }
    
    public String getIdpName() {
        return idpName;
    }
    
    public void setIdpName(String idpName) {
        this.idpName = idpName;
    }
    
    public String getIdpEntityId() {
        return idpEntityId;
    }
    
    public void setIdpEntityId(String idpEntityId) {
        this.idpEntityId = idpEntityId;
    }
    
    public String getIdpSsoUrl() {
        return idpSsoUrl;
    }
    
    public void setIdpSsoUrl(String idpSsoUrl) {
        this.idpSsoUrl = idpSsoUrl;
    }
    
    public String getIdpSloUrl() {
        return idpSloUrl;
    }
    
    public void setIdpSloUrl(String idpSloUrl) {
        this.idpSloUrl = idpSloUrl;
    }
    
    public String getIdpCertificate() {
        return idpCertificate;
    }
    
    public void setIdpCertificate(String idpCertificate) {
        this.idpCertificate = idpCertificate;
    }
    
    public String getLogoUrl() {
        return logoUrl;
    }
    
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
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
    
    public List<IdpProperty> getProperties() {
        return properties;
    }
    
    public void setProperties(List<IdpProperty> properties) {
        this.properties = properties;
    }
    
    public List<IdpEmailDomain> getEmailDomains() {
        return emailDomains;
    }
    
    public void setEmailDomains(List<IdpEmailDomain> emailDomains) {
        this.emailDomains = emailDomains;
    }
    
    // Helper methods
    public void addProperty(IdpProperty property) {
        properties.add(property);
        property.setIdpConfiguration(this);
    }
    
    public void addEmailDomain(IdpEmailDomain emailDomain) {
        emailDomains.add(emailDomain);
        emailDomain.setIdpConfiguration(this);
    }
    
    public String getPropertyValue(String propertyName) {
        return properties.stream()
                .filter(p -> p.getPropertyName().equals(propertyName))
                .map(IdpProperty::getPropertyValue)
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public String toString() {
        return "IdpConfiguration{" +
                "id=" + id +
                ", idpId='" + idpId + '\'' +
                ", idpName='" + idpName + '\'' +
                ", isActive=" + isActive +
                ", isDefault=" + isDefault +
                '}';
    }
} 