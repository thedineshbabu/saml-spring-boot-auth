package com.example.service;

import com.example.entity.IdpConfiguration;
import com.example.entity.IdpEmailDomain;
import com.example.entity.IdpProperty;
import com.example.repository.IdpConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for IdP configuration management.
 * 
 * Provides business logic for managing Identity Provider configurations,
 * including CRUD operations, email domain mapping, and SAML property management.
 * 
 * @author SAML Spring Boot Application
 * @version 1.0.0
 */
@Service
@Transactional
public class IdpConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(IdpConfigurationService.class);
    
    private final IdpConfigurationRepository idpConfigurationRepository;
    
    @Autowired
    public IdpConfigurationService(IdpConfigurationRepository idpConfigurationRepository) {
        this.idpConfigurationRepository = idpConfigurationRepository;
    }
    
    /**
     * Get all active IdP configurations.
     * 
     * @return List of active IdP configurations
     */
    @Cacheable("idpConfigurations")
    public List<IdpConfiguration> getAllActiveIdpConfigurations() {
        logger.debug("Fetching all active IdP configurations");
        List<IdpConfiguration> configurations = idpConfigurationRepository.findByIsActiveTrue();
        logger.info("Found {} active IdP configurations", configurations.size());
        return configurations;
    }
    
    /**
     * Get IdP configuration by ID.
     * 
     * @param id the IdP configuration ID
     * @return Optional containing the IdP configuration if found
     */
    @Cacheable("idpConfiguration")
    public Optional<IdpConfiguration> getById(Integer id) {
        logger.debug("Fetching IdP configuration with ID: {}", id);
        return idpConfigurationRepository.findById(id);
    }
    
    /**
     * Get IdP configuration by IdP ID.
     * 
     * @param idpId the IdP identifier
     * @return Optional containing the IdP configuration if found
     */
    @Cacheable("idpConfiguration")
    public Optional<IdpConfiguration> getByIdpId(String idpId) {
        logger.debug("Fetching IdP configuration with IdP ID: {}", idpId);
        return idpConfigurationRepository.findByIdpId(idpId);
    }
    
    /**
     * Get the default IdP configuration.
     * 
     * @return Optional containing the default IdP configuration if found
     */
    @Cacheable("defaultIdpConfiguration")
    public Optional<IdpConfiguration> getDefaultIdpConfiguration() {
        logger.debug("Fetching default IdP configuration");
        return idpConfigurationRepository.findByIsDefaultTrue();
    }
    
    /**
     * Find IdP configuration by email domain.
     * 
     * @param email the email address to find IdP for
     * @return Optional containing the IdP configuration if found
     */
    @Cacheable("idpConfigurationByEmail")
    public Optional<IdpConfiguration> findByIdpByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.warn("Email is null or empty, cannot find IdP configuration");
            return Optional.empty();
        }
        
        String domain = extractDomainFromEmail(email);
        logger.debug("Finding IdP configuration for email domain: {}", domain);
        
        Optional<IdpConfiguration> configuration = idpConfigurationRepository.findByEmailDomain(domain);
        
        if (configuration.isPresent()) {
            logger.info("Found IdP configuration '{}' for email domain: {}", 
                       configuration.get().getIdpName(), domain);
        } else {
            logger.warn("No IdP configuration found for email domain: {}", domain);
        }
        
        return configuration;
    }
    
    /**
     * Get all IdP configurations with full details.
     * 
     * @return List of IdP configurations with properties and email domains
     */
    @Cacheable("idpConfigurationsWithDetails")
    public List<IdpConfiguration> getAllActiveWithDetails() {
        logger.debug("Fetching all active IdP configurations with details");
        List<IdpConfiguration> configurations = idpConfigurationRepository.findAllActiveWithDetails();
        logger.info("Found {} active IdP configurations with details", configurations.size());
        return configurations;
    }
    
    /**
     * Create a new IdP configuration.
     * 
     * @param configuration the IdP configuration to create
     * @return the created IdP configuration
     */
    @CacheEvict(value = {"idpConfigurations", "idpConfigurationsWithDetails"}, allEntries = true)
    public IdpConfiguration createIdpConfiguration(IdpConfiguration configuration) {
        logger.info("Creating new IdP configuration: {}", configuration.getIdpId());
        
        // Validate configuration
        validateIdpConfiguration(configuration);
        
        // Set default values
        if (configuration.getIsActive() == null) {
            configuration.setIsActive(true);
        }
        if (configuration.getIsDefault() == null) {
            configuration.setIsDefault(false);
        }
        
        IdpConfiguration savedConfiguration = idpConfigurationRepository.save(configuration);
        logger.info("Successfully created IdP configuration with ID: {}", savedConfiguration.getId());
        
        return savedConfiguration;
    }
    
    /**
     * Update an existing IdP configuration.
     * 
     * @param id the IdP configuration ID
     * @param configuration the updated configuration
     * @return Optional containing the updated IdP configuration if found
     */
    @CacheEvict(value = {"idpConfigurations", "idpConfigurationsWithDetails", "idpConfiguration", "defaultIdpConfiguration"}, allEntries = true)
    public Optional<IdpConfiguration> updateIdpConfiguration(Integer id, IdpConfiguration configuration) {
        logger.info("Updating IdP configuration with ID: {}", id);
        
        return idpConfigurationRepository.findById(id)
                .map(existingConfig -> {
                    // Update basic fields
                    existingConfig.setIdpName(configuration.getIdpName());
                    existingConfig.setLogoUrl(configuration.getLogoUrl());
                    existingConfig.setDisplayName(configuration.getDisplayName());
                    existingConfig.setIsActive(configuration.getIsActive());
                    existingConfig.setIsDefault(configuration.getIsDefault());
                    
                    // TODO: Update SAML-specific properties in the properties table
                    // This will be implemented when we add property management
                    
                    IdpConfiguration updatedConfig = idpConfigurationRepository.save(existingConfig);
                    logger.info("Successfully updated IdP configuration with ID: {}", updatedConfig.getId());
                    
                    return updatedConfig;
                });
    }
    
    /**
     * Delete an IdP configuration.
     * 
     * @param id the IdP configuration ID
     * @return true if the configuration was deleted
     */
    @CacheEvict(value = {"idpConfigurations", "idpConfigurationsWithDetails", "idpConfiguration", "defaultIdpConfiguration"}, allEntries = true)
    public boolean deleteIdpConfiguration(Integer id) {
        logger.info("Deleting IdP configuration with ID: {}", id);
        
        if (idpConfigurationRepository.existsById(id)) {
            idpConfigurationRepository.deleteById(id);
            logger.info("Successfully deleted IdP configuration with ID: {}", id);
            return true;
        } else {
            logger.warn("IdP configuration with ID {} not found for deletion", id);
            return false;
        }
    }
    
    /**
     * Add a property to an IdP configuration.
     * 
     * @param idpId the IdP identifier
     * @param propertyName the property name
     * @param propertyValue the property value
     * @return true if the property was added successfully
     */
    @CacheEvict(value = {"idpConfigurations", "idpConfigurationsWithDetails", "idpConfiguration"}, allEntries = true)
    public boolean addProperty(String idpId, String propertyName, String propertyValue) {
        logger.debug("Adding property '{}' to IdP configuration: {}", propertyName, idpId);
        
        return idpConfigurationRepository.findByIdpId(idpId)
                .map(configuration -> {
                    IdpProperty property = new IdpProperty(propertyName, propertyValue);
                    configuration.addProperty(property);
                    idpConfigurationRepository.save(configuration);
                    logger.info("Successfully added property '{}' to IdP configuration: {}", propertyName, idpId);
                    return true;
                })
                .orElse(false);
    }
    
    /**
     * Add an email domain to an IdP configuration.
     * 
     * @param idpId the IdP identifier
     * @param emailDomain the email domain
     * @return true if the email domain was added successfully
     */
    @CacheEvict(value = {"idpConfigurations", "idpConfigurationsWithDetails", "idpConfiguration", "idpConfigurationByEmail"}, allEntries = true)
    public boolean addEmailDomain(String idpId, String emailDomain) {
        logger.debug("Adding email domain '{}' to IdP configuration: {}", emailDomain, idpId);
        
        return idpConfigurationRepository.findByIdpId(idpId)
                .map(configuration -> {
                    IdpEmailDomain domain = new IdpEmailDomain(emailDomain, true);
                    configuration.addEmailDomain(domain);
                    idpConfigurationRepository.save(configuration);
                    logger.info("Successfully added email domain '{}' to IdP configuration: {}", emailDomain, idpId);
                    return true;
                })
                .orElse(false);
    }
    
    /**
     * Get SAML properties for an IdP configuration.
     * 
     * @param idpId the IdP identifier
     * @return Map of property names to values
     */
    public Map<String, String> getSamlProperties(String idpId) {
        logger.debug("Getting SAML properties for IdP configuration: {}", idpId);
        
        return idpConfigurationRepository.findByIdpId(idpId)
                .map(configuration -> configuration.getProperties().stream()
                        .collect(Collectors.toMap(
                                IdpProperty::getPropertyName,
                                IdpProperty::getPropertyValue,
                                (existing, replacement) -> existing)))
                .orElse(new HashMap<>());
    }
    
    /**
     * Extract domain from email address.
     * 
     * @param email the email address
     * @return the domain part of the email
     */
    private String extractDomainFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "";
        }
        return email.substring(email.indexOf("@") + 1).toLowerCase();
    }
    
    /**
     * Validate IdP configuration.
     * 
     * @param configuration the IdP configuration to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateIdpConfiguration(IdpConfiguration configuration) {
        if (configuration.getIdpId() == null || configuration.getIdpId().trim().isEmpty()) {
            throw new IllegalArgumentException("IdP ID is required");
        }
        if (configuration.getIdpName() == null || configuration.getIdpName().trim().isEmpty()) {
            throw new IllegalArgumentException("IdP name is required");
        }
        
        // Check for duplicate IdP ID
        if (idpConfigurationRepository.existsByIdpId(configuration.getIdpId())) {
            throw new IllegalArgumentException("IdP ID already exists: " + configuration.getIdpId());
        }
    }
} 