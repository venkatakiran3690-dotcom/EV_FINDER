package com.smartev.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class ProductionConfigurationValidatorTest {

    private static final String DEFAULT_DEV_JWT = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final String STRONG_PROD_JWT = "a9b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4e3f2a1b0c9d8e7f6a5b4c3d2e1f0a9b8";

    @Test
    @DisplayName("Development profile allows default demo credentials")
    void testDevelopmentProfilePermitsDefaultCredentials() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev-h2");

        ProductionConfigurationValidator validator =
                new ProductionConfigurationValidator(env, DEFAULT_DEV_JWT, "root");

        assertDoesNotThrow(validator::validate);
    }

    @Test
    @DisplayName("Production profile rejects default development JWT secret")
    void testProductionProfileRejectsDefaultJwtSecret() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        ProductionConfigurationValidator validator =
                new ProductionConfigurationValidator(env, DEFAULT_DEV_JWT, "StrongProdPass#2026");

        IllegalStateException ex = assertThrows(IllegalStateException.class, validator::validate);
        assertTrue(ex.getMessage().contains("default development JWT secret cannot be used in production"));
    }

    @Test
    @DisplayName("Production profile rejects insecure/default database password")
    void testProductionProfileRejectsDefaultDbPassword() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        ProductionConfigurationValidator validator =
                new ProductionConfigurationValidator(env, STRONG_PROD_JWT, "root");

        IllegalStateException ex = assertThrows(IllegalStateException.class, validator::validate);
        assertTrue(ex.getMessage().contains("Default or trivial database password"));
    }

    @Test
    @DisplayName("Production profile passes when secure custom credentials are provided")
    void testProductionProfilePassesWithSecureCredentials() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        ProductionConfigurationValidator validator =
                new ProductionConfigurationValidator(env, STRONG_PROD_JWT, "StrongProdDatabasePassword#9876!");

        assertDoesNotThrow(validator::validate);
    }
}
