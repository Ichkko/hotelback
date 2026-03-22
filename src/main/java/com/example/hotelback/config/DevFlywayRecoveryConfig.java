package com.example.hotelback.config;

import org.flywaydb.core.api.FlywayException;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevFlywayRecoveryConfig {

    @Bean
    public FlywayMigrationStrategy devFlywayMigrationStrategy() {
        return flyway -> {
            try {
                flyway.migrate();
            } catch (FlywayException ex) {
                String message = ex.getMessage();
                if (message != null && message.contains("Migrations have failed validation")) {
                    flyway.repair();
                    flyway.migrate();
                    return;
                }
                throw ex;
            }
        };
    }
}
