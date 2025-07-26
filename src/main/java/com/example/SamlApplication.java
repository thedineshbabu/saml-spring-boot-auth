package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot application class for SAML authentication service.
 * 
 * This application provides SAML-based Single Sign-On (SSO) functionality
 * with Azure AD integration, replacing the servlet-based implementation
 * with a modern Spring Boot architecture.
 * 
 * @author SAML Spring Boot Application
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.repository")
@EnableConfigurationProperties
@EnableAsync
public class SamlApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(SamlApplication.class);
    
    /**
     * Main method to start the Spring Boot SAML application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        logger.info("Starting Spring Boot SAML Application...");
        
        try {
            SpringApplication app = new SpringApplication(SamlApplication.class);
            app.run(args);
            
            logger.info("Spring Boot SAML Application started successfully!");
            logger.info("Application is running on: http://localhost:8080");
            logger.info("SAML Metadata available at: http://localhost:8080/saml2/service-provider-metadata");
            
        } catch (Exception e) {
            logger.error("Failed to start Spring Boot SAML Application: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
} 