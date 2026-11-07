package com.smartev.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Component
public class ProductionConfigurationValidator {

    private static final Logger log = LoggerFactory.getLogger(ProductionConfigurationValidator.class);
    private static final String DEFAULT_DEV_JWT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final List<String> INSECURE_PASSWORDS = List.of("root", "admin", "password", "123456", "");

    private final Environment environment;
    private final String jwtSecret;
    private final String dbPassword;

    public ProductionConfigurationValidator(
            Environment environment,
            @Value("${app.jwt.secret:}") String jwtSecret,
            @Value("${spring.datasource.password:}") String dbPassword) {
        this.environment = environment;
        this.jwtSecret = jwtSecret;
        this.dbPassword = dbPassword;
    }

    @PostConstruct
    public void validate() {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        boolean isProduction = activeProfiles.contains("prod") || activeProfiles.contains("production");

        if (isProduction) {
            log.info("Validating production environment configuration...");

            // 1. JWT Secret Validation
            if (jwtSecret == null || jwtSecret.isBlank()) {
                throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: [app.jwt.secret] cannot be blank in production. Provide a secure JWT_SECRET environment variable.");
            }
            if (DEFAULT_DEV_JWT_SECRET.equals(jwtSecret)) {
                throw new IllegalStateException("CRITICAL SECURITY ERROR: The default development JWT secret cannot be used in production profile. Set a secure JWT_SECRET environment variable.");
            }
            if (jwtSecret.length() < 32) {
                throw new IllegalStateException("CRITICAL SECURITY ERROR: Production JWT secret must be at least 256 bits (32 characters). Set a secure JWT_SECRET environment variable.");
            }

            // 2. Database Password Validation
            if (dbPassword == null || dbPassword.isBlank()) {
                throw new IllegalStateException("CRITICAL CONFIGURATION ERROR: [spring.datasource.password] cannot be blank in production. Provide a secure SPRING_DATASOURCE_PASSWORD environment variable.");
            }
            if (INSECURE_PASSWORDS.contains(dbPassword.toLowerCase())) {
                throw new IllegalStateException("CRITICAL SECURITY ERROR: Default or trivial database password ('" + dbPassword + "') is forbidden in production. Provide a secure SPRING_DATASOURCE_PASSWORD environment variable.");
            }

            log.info("Production configuration validation passed: Secrets and credentials are appropriately secured.");
        }
    }
}
