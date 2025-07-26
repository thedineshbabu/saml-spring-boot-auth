package com.example.repository;

import com.example.entity.IdpConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for IdP configuration management.
 * 
 * Provides data access methods for Identity Provider configurations
 * with support for custom queries and business logic.
 * 
 * @author SAML Spring Boot Application
 * @version 1.0.0
 */
@Repository
public interface IdpConfigurationRepository extends JpaRepository<IdpConfiguration, Long> {
    
    /**
     * Find IdP configuration by IdP ID.
     * 
     * @param idpId the IdP identifier
     * @return Optional containing the IdP configuration if found
     */
    Optional<IdpConfiguration> findByIdpId(String idpId);
    
    /**
     * Find all active IdP configurations.
     * 
     * @return List of active IdP configurations
     */
    List<IdpConfiguration> findByIsActiveTrue();
    
    /**
     * Find the default IdP configuration.
     * 
     * @return Optional containing the default IdP configuration if found
     */
    Optional<IdpConfiguration> findByIsDefaultTrue();
    
    /**
     * Find IdP configuration by entity ID.
     * 
     * @param entityId the SAML entity ID
     * @return Optional containing the IdP configuration if found
     */
    Optional<IdpConfiguration> findByIdpEntityId(String entityId);
    
    /**
     * Find IdP configuration by email domain.
     * 
     * @param emailDomain the email domain to search for
     * @return Optional containing the IdP configuration if found
     */
    @Query("SELECT ic FROM IdpConfiguration ic " +
           "JOIN ic.emailDomains ed " +
           "WHERE ed.emailDomain = :emailDomain " +
           "AND ed.isActive = true " +
           "AND ic.isActive = true")
    Optional<IdpConfiguration> findByEmailDomain(@Param("emailDomain") String emailDomain);
    
    /**
     * Find all IdP configurations with their properties and email domains.
     * 
     * @return List of IdP configurations with all related data
     */
    @Query("SELECT DISTINCT ic FROM IdpConfiguration ic " +
           "LEFT JOIN FETCH ic.properties " +
           "LEFT JOIN FETCH ic.emailDomains " +
           "WHERE ic.isActive = true")
    List<IdpConfiguration> findAllActiveWithDetails();
    
    /**
     * Check if an IdP configuration exists by IdP ID.
     * 
     * @param idpId the IdP identifier
     * @return true if the IdP configuration exists
     */
    boolean existsByIdpId(String idpId);
    
    /**
     * Find IdP configurations by partial name match.
     * 
     * @param namePattern the name pattern to search for
     * @return List of matching IdP configurations
     */
    @Query("SELECT ic FROM IdpConfiguration ic " +
           "WHERE ic.idpName LIKE %:namePattern% " +
           "AND ic.isActive = true")
    List<IdpConfiguration> findByNameContaining(@Param("namePattern") String namePattern);
    
    /**
     * Count active IdP configurations.
     * 
     * @return number of active IdP configurations
     */
    long countByIsActiveTrue();
    
    /**
     * Find IdP configurations created after a specific date.
     * 
     * @param date the date to compare against
     * @return List of IdP configurations created after the date
     */
    List<IdpConfiguration> findByCreatedAtAfter(java.time.LocalDateTime date);
} 