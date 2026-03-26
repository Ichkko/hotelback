package com.example.hotelback.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevFlywayRepairConfig {

    @Bean
    FlywayMigrationStrategy devFlywayMigrationStrategy() {
        return flyway -> migrateWithRepairFallback(flyway);
    }

    private void migrateWithRepairFallback(Flyway flyway) {
        try {
            flyway.migrate();
        } catch (FlywayException exception) {
            if (!isRepairableValidationFailure(exception)) {
                throw exception;
            }

            flyway.repair();
            flyway.migrate();
        }
    }

    private boolean isRepairableValidationFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && (
                    message.contains("Migration checksum mismatch")
                            || message.contains("Detected failed migration to version"))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
