package com.example.hotelback.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("prod")
public class ProductionEnvironmentValidator implements InitializingBean {

    private static final String[] REQUIRED_VARIABLES = {
            "DB_URL",
            "DB_USERNAME",
            "DB_PASSWORD",
            "JWT_TOKEN"
    };

    private final Environment environment;

    public ProductionEnvironmentValidator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() {
        List<String> missingVariables = new ArrayList<>();

        for (String requiredVariable : REQUIRED_VARIABLES) {
            String value = environment.getProperty(requiredVariable);
            if (value == null || value.isBlank()) {
                missingVariables.add(requiredVariable);
            }
        }

        if (!missingVariables.isEmpty()) {
            throw new IllegalStateException(
                    "Missing required production environment variables: " + String.join(", ", missingVariables)
                            + ". Provide them via your deployment environment or secret manager."
            );
        }
    }
}
