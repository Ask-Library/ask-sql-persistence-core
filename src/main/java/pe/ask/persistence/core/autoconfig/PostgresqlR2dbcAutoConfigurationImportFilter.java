package pe.ask.persistence.core.autoconfig;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Objects;

/**
 * Excludes Spring Boot's default R2DBC auto-configuration when
 * Ask Persistence manages PostgreSQL connections.
 *
 * @author Allan Sagastegui
 */
public final class PostgresqlR2dbcAutoConfigurationImportFilter
        implements AutoConfigurationImportFilter, EnvironmentAware {

    private static final String ENABLED_PROPERTY =
            "pe.ask.persistence.enabled";

    private static final String DRIVER_PROPERTY =
            "pe.ask.persistence.db-driver";

    private static final String POSTGRESQL_DRIVER =
            "postgresql";

    private static final String R2DBC_AUTO_CONFIGURATION =
            R2dbcAutoConfiguration.class.getName();

    private Environment environment;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public boolean[] match(
            String[] autoConfigurationClasses,
            @NonNull AutoConfigurationMetadata autoConfigurationMetadata
    ) {
        boolean[] matches =
                new boolean[autoConfigurationClasses.length];

        Arrays.fill(matches, true);

        if (!shouldExcludeR2dbcAutoConfiguration()) {
            return matches;
        }

        for (int index = 0;
             index < autoConfigurationClasses.length;
             index++) {

            String autoConfiguration =
                    autoConfigurationClasses[index];

            if (R2DBC_AUTO_CONFIGURATION.equals(
                    autoConfiguration
            )) {
                matches[index] = false;
            }
        }

        return matches;
    }

    private boolean shouldExcludeR2dbcAutoConfiguration() {
        Environment currentEnvironment =
                Objects.requireNonNull(
                        environment,
                        "Environment must be initialized"
                );

        boolean enabled = currentEnvironment.getProperty(
                ENABLED_PROPERTY,
                Boolean.class,
                true
        );

        String driver = currentEnvironment.getProperty(
                DRIVER_PROPERTY,
                POSTGRESQL_DRIVER
        );

        return enabled
                && POSTGRESQL_DRIVER.equalsIgnoreCase(driver);
    }
}