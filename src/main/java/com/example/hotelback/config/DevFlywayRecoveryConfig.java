package com.example.hotelback.config;

import org.flywaydb.core.api.exception.FlywayValidateException;
import org.flywaydb.core.api.output.ValidateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
@ConditionalOnProperty(
        prefix = "app.flyway",
        name = "repair-on-validation-error",
        havingValue = "true",
        matchIfMissing = true
)
public class DevFlywayRecoveryConfig {

    private static final Logger log = LoggerFactory.getLogger(DevFlywayRecoveryConfig.class);

    @Bean
    public FlywayMigrationStrategy devFlywayMigrationStrategy() {
        return flyway -> {
            try {
                flyway.migrate();
            } catch (FlywayValidateException ex) {
                if (containsFailedMigration(ex)) {
                    log.warn("Flyway validation failed because a migration is marked as failed. Repairing schema history and retrying once in the dev profile.");
                    flyway.repair();
                    flyway.migrate();
                    return;
                }
                throw ex;
            }
        };
    }

    private boolean containsFailedMigration(FlywayValidateException ex) {
        ValidateResult validateResult = ex.getValidateResult();
        return validateResult != null
                && validateResult.getAllErrorMessages() != null
                && validateResult.getAllErrorMessages().contains("Detected failed migration");
    }
}
