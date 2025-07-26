package com.example.controller;

import com.example.entity.IdpConfiguration;
import com.example.service.IdpConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

/**
 * Authentication controller for SAML-based authentication.
 * 
 * Handles login, dashboard, and SAML-related endpoints,
 * replacing servlet-based authentication with Spring MVC.
 * 
 * @author SAML Spring Boot Application
 * @version 1.0.0
 */
@Controller
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final IdpConfigurationService idpConfigurationService;
    
    @Autowired
    public AuthController(IdpConfigurationService idpConfigurationService) {
        this.idpConfigurationService = idpConfigurationService;
    }
    
    /**
     * Handle root endpoint - redirect to login.
     * 
     * @return redirect to login page
     */
    @GetMapping("/")
    public String root() {
        logger.debug("Root endpoint accessed, redirecting to login");
        return "redirect:/login";
    }
    
    /**
     * Display login page with available IdP options.
     * 
     * @param model the Spring MVC model
     * @param request the HTTP request
     * @param error error parameter from URL
     * @param logout logout parameter from URL
     * @param expired expired parameter from URL
     * @return login page view name
     */
    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request, 
                       @RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       @RequestParam(value = "expired", required = false) String expired) {
        
        logger.debug("Login page requested from IP: {}", request.getRemoteAddr());
        
        // Check if user is already authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            logger.info("User already authenticated, redirecting to dashboard");
            return "redirect:/dashboard";
        }
        
        // Add error messages to model
        if (error != null) {
            model.addAttribute("error", "Authentication failed. Please try again.");
            logger.warn("Login error detected");
        }
        
        if (logout != null) {
            model.addAttribute("message", "You have been successfully logged out.");
            logger.info("Logout message displayed");
        }
        
        if (expired != null) {
            model.addAttribute("error", "Your session has expired. Please login again.");
            logger.warn("Session expired message displayed");
        }
        
        // Get available IdP configurations
        List<IdpConfiguration> idpConfigurations = idpConfigurationService.getAllActiveIdpConfigurations();
        model.addAttribute("idpConfigurations", idpConfigurations);
        
        logger.info("Login page displayed with {} IdP configurations", idpConfigurations.size());
        return "login";
    }
    
    /**
     * Handle email-based IdP selection.
     * 
     * @param email the user's email address
     * @param model the model to add attributes to
     * @param request the HTTP request
     * @return redirect to appropriate IdP or login page with error
     */
    @PostMapping("/login")
    public String processLogin(@RequestParam("email") String email, 
                              Model model, 
                              HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        logger.info("Email-based login attempt for email: {} from IP: {}", email, clientIp);
        
        try {
            if (email == null || email.trim().isEmpty()) {
                logger.warn("Empty email provided for login");
                model.addAttribute("error", "Please enter a valid email address.");
                model.addAttribute("email", email);
                model.addAttribute("idpConfigurations", idpConfigurationService.getAllActiveIdpConfigurations());
                return "login";
            }
            
            // Extract domain from email
            String domain = extractDomainFromEmail(email.trim());
            if (domain == null) {
                logger.warn("Invalid email format: {}", email);
                model.addAttribute("error", "Please enter a valid email address.");
                model.addAttribute("email", email);
                model.addAttribute("idpConfigurations", idpConfigurationService.getAllActiveIdpConfigurations());
                return "login";
            }
            
            // Find IdP configuration for the email domain
            Optional<IdpConfiguration> idpConfig = idpConfigurationService.findByIdpByEmail(email);
            
            if (idpConfig.isPresent()) {
                IdpConfiguration config = idpConfig.get();
                logger.info("Found IdP configuration '{}' for domain '{}': {}", domain, config.getIdpName(), email);
                
                // Store email in session for later use
                HttpSession session = request.getSession();
                session.setAttribute("userEmail", email);
                session.setAttribute("selectedIdp", config.getIdpId());
                
                // Redirect to SAML authentication for the found IdP
                String redirectUrl = "/saml2/authenticate/" + config.getIdpId();
                logger.info("Redirecting to SAML authentication: {}", redirectUrl);
                return "redirect:" + redirectUrl;
            } else {
                logger.warn("No IdP configuration found for domain: {}", domain);
                model.addAttribute("error", "No identity provider configured for your email domain. Please contact your administrator or choose from the available providers below.");
                model.addAttribute("email", email);
                model.addAttribute("idpConfigurations", idpConfigurationService.getAllActiveIdpConfigurations());
                return "login";
            }
            
        } catch (Exception e) {
            logger.error("Error processing email-based login: {}", e.getMessage(), e);
            model.addAttribute("error", "An error occurred while processing your request. Please try again.");
            model.addAttribute("email", email);
            model.addAttribute("idpConfigurations", idpConfigurationService.getAllActiveIdpConfigurations());
            return "login";
        }
    }
    
    /**
     * Display dashboard for authenticated users.
     * 
     * @param model the Spring MVC model
     * @param request the HTTP request
     * @return dashboard page view name
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpServletRequest request) {
        logger.debug("Dashboard requested from IP: {}", request.getRemoteAddr());
        
        // Get current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            logger.warn("Unauthenticated user attempting to access dashboard");
            return "redirect:/login";
        }
        
        // Extract user information from SAML authentication
        if (authentication instanceof Saml2Authentication) {
            Saml2Authentication samlAuth = (Saml2Authentication) authentication;
            String username = samlAuth.getName();
            String email = extractEmailFromSamlAttributes(samlAuth);
            
            model.addAttribute("username", username);
            model.addAttribute("email", email);
                               model.addAttribute("samlAttributes", null); // TODO: Fix SAML attributes access
            
            logger.info("Dashboard displayed for user: {} ({})", username, email);
        } else {
            // Fallback for non-SAML authentication
            model.addAttribute("username", authentication.getName());
            model.addAttribute("email", "N/A");
            model.addAttribute("samlAttributes", null);
            
            logger.info("Dashboard displayed for user: {}", authentication.getName());
        }
        
        // Add available IdP configurations for logout options
        List<IdpConfiguration> idpConfigurations = idpConfigurationService.getAllActiveIdpConfigurations();
        model.addAttribute("idpConfigurations", idpConfigurations);
        
        return "dashboard";
    }
    
    /**
     * Handle SAML metadata endpoint.
     * 
     * @param registrationId the SAML registration ID
     * @param model the Spring MVC model
     * @return metadata view name
     */
    @GetMapping("/saml2/service-provider-metadata/{registrationId}")
    public String metadata(@PathVariable String registrationId, Model model) {
        logger.debug("SAML metadata requested for registration: {}", registrationId);
        
        Optional<IdpConfiguration> idpConfig = idpConfigurationService.getByIdpId(registrationId);
        
        if (idpConfig.isPresent()) {
            model.addAttribute("idpConfiguration", idpConfig.get());
            logger.info("SAML metadata displayed for IdP: {}", idpConfig.get().getIdpName());
            return "metadata";
        } else {
            logger.warn("SAML metadata requested for unknown registration: {}", registrationId);
            return "error/404";
        }
    }
    
    /**
     * Handle logout with optional IdP selection.
     * 
     * @param idpId the IdP ID for logout (optional)
     * @param simple whether to perform simple logout (optional)
     * @param request the HTTP request
     * @return redirect to logout or login page
     */
    @GetMapping("/logout")
    public String logout(@RequestParam(value = "idp", required = false) String idpId,
                        @RequestParam(value = "simple", required = false) String simple,
                        HttpServletRequest request) {
        
        logger.info("Logout requested - IdP: {}, Simple: {}", idpId, simple);
        
        if ("true".equals(simple)) {
            // Perform simple logout (local session invalidation only)
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
                logger.info("Simple logout completed - session invalidated");
            }
            return "redirect:/login?logout=true";
        }
        
        if (idpId != null && !idpId.trim().isEmpty()) {
            // Redirect to specific IdP logout
            Optional<IdpConfiguration> idpConfig = idpConfigurationService.getByIdpId(idpId);
            if (idpConfig.isPresent()) {
                String sloUrl = idpConfig.get().getPropertyValue("idp_slo_url");
                if (sloUrl != null) {
                    logger.info("Redirecting to IdP logout: {}", idpConfig.get().getIdpName());
                    return "redirect:" + sloUrl;
                }
            }
        }
        
        // Default logout (handled by Spring Security)
        logger.info("Performing default logout");
        return "redirect:/login?logout=true";
    }
    
    /**
     * Extract email from SAML attributes.
     * 
     * @param samlAuth the SAML authentication object
     * @return email address or null if not found
     */
    private String extractEmailFromSamlAttributes(Saml2Authentication samlAuth) {
        try {
            // Try common SAML attribute names for email
            String[] emailAttributes = {"email", "mail", "user.email", "userprincipalname"};
            
                        // TODO: Fix SAML attributes access
            logger.warn("SAML attributes access not implemented yet");
            return null;
            
        } catch (Exception e) {
            logger.error("Error extracting email from SAML attributes: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extract domain from email address.
     * 
     * @param email the email address
     * @return the domain part of the email or null if invalid
     */
    private String extractDomainFromEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Simple email validation and domain extraction
            String trimmedEmail = email.trim().toLowerCase();
            
            // Check if it's a valid email format
            if (!trimmedEmail.contains("@") || trimmedEmail.startsWith("@") || trimmedEmail.endsWith("@")) {
                return null;
            }
            
            // Extract domain part
            String domain = trimmedEmail.substring(trimmedEmail.indexOf("@") + 1);
            
            // Basic domain validation
            if (domain.isEmpty() || domain.contains("@") || domain.startsWith(".") || domain.endsWith(".")) {
                return null;
            }
            
            logger.debug("Extracted domain '{}' from email '{}'", domain, email);
            return domain;
            
        } catch (Exception e) {
            logger.error("Error extracting domain from email '{}': {}", e.getMessage());
            return null;
        }
    }
} 