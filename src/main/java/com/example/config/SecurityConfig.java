package com.example.config;

import com.example.service.IdpConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.List;

/**
 * Spring Security configuration for SAML authentication.
 * 
 * Configures SAML-based authentication with support for multiple
 * Identity Providers and dynamic configuration from database.
 * 
 * @author SAML Spring Boot Application
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    
    private final IdpConfigurationService idpConfigurationService;
    
    @Autowired
    public SecurityConfig(IdpConfigurationService idpConfigurationService) {
        this.idpConfigurationService = idpConfigurationService;
    }
    
    /**
     * Configure security filter chain with SAML authentication.
     * 
     * @param http the HttpSecurity object
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring Spring Security with SAML authentication");
        
        http
            // Disable CSRF for SAML endpoints
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/saml2/service-provider-metadata/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // Protected endpoints
                .requestMatchers("/dashboard/**").authenticated()
                .requestMatchers("/api/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Configure SAML authentication
            .saml2Login(saml2 -> saml2
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository())
            )
            
            // Configure SAML logout
            .saml2Logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            
            // Configure session management
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            )
            
            // Configure exception handling
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/error/403")
                .authenticationEntryPoint((request, response, authException) -> {
                    logger.warn("Authentication required for: {}", request.getRequestURI());
                    response.sendRedirect("/login");
                })
            );
        
        logger.info("Spring Security configuration completed successfully");
        return http.build();
    }
    
    /**
     * Configure Relying Party Registration Repository for SAML.
     * 
     * @return RelyingPartyRegistrationRepository
     */
    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        logger.info("Initializing SAML Relying Party Registration Repository");
        
        return new RelyingPartyRegistrationRepository() {
            @Override
            public RelyingPartyRegistration findByRegistrationId(String registrationId) {
                logger.debug("Looking up SAML registration for ID: {}", registrationId);
                
                return idpConfigurationService.getByIdpId(registrationId)
                    .map(this::createRelyingPartyRegistration)
                    .orElse(null);
            }
            
            @Override
            public Iterable<RelyingPartyRegistration> iterator() {
                logger.debug("Retrieving all SAML registrations");
                
                List<RelyingPartyRegistration> registrations = idpConfigurationService
                    .getAllActiveIdpConfigurations()
                    .stream()
                    .map(this::createRelyingPartyRegistration)
                    .toList();
                
                logger.info("Found {} active SAML registrations", registrations.size());
                return registrations;
            }
        };
    }
    
    /**
     * Create RelyingPartyRegistration from IdP configuration.
     * 
     * @param idpConfig the IdP configuration
     * @return RelyingPartyRegistration
     */
    private RelyingPartyRegistration createRelyingPartyRegistration(com.example.entity.IdpConfiguration idpConfig) {
        logger.debug("Creating SAML registration for IdP: {}", idpConfig.getIdpName());
        
        try {
            return RelyingPartyRegistration
                .withRegistrationId(idpConfig.getIdpId())
                .assertionConsumerServiceLocation("http://localhost:8080/login/saml2/sso/" + idpConfig.getIdpId())
                .entityId("http://localhost:8080/saml2/service-provider-metadata/" + idpConfig.getIdpId())
                .signingX509Credentials(credentials -> credentials.add(getSigningCredential()))
                .assertingPartyDetails(party -> party
                    .entityId(idpConfig.getIdpEntityId())
                    .singleSignOnServiceLocation(idpConfig.getIdpSsoUrl())
                    .singleSignOnServiceBinding(Saml2MessageBinding.HTTP_REDIRECT)
                    .wantAuthnRequestsSigned(false)
                )
                .build();
                
        } catch (Exception e) {
            logger.error("Failed to create SAML registration for IdP {}: {}", 
                        idpConfig.getIdpName(), e.getMessage(), e);
            throw new RuntimeException("SAML registration creation failed", e);
        }
    }
    
    /**
     * Get signing credential for SAML requests.
     * 
     * @return Saml2X509Credential
     */
    private org.springframework.security.saml2.credentials.Saml2X509Credential getSigningCredential() {
        // TODO: Implement certificate loading from configuration
        // For now, return a placeholder credential
        logger.warn("Using placeholder SAML signing credential - implement certificate loading");
        
        try {
            // This is a placeholder - in production, load from keystore or configuration
            return org.springframework.security.saml2.credentials.Saml2X509Credential.signing(
                java.security.KeyFactory.getInstance("RSA").generatePrivate(null),
                java.security.cert.CertificateFactory.getInstance("X.509").generateCertificate(null)
            );
        } catch (Exception e) {
            logger.error("Failed to create SAML signing credential: {}", e.getMessage(), e);
            throw new RuntimeException("SAML credential creation failed", e);
        }
    }
} 