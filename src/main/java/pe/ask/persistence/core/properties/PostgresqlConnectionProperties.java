package pe.ask.persistence.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

@ConfigurationProperties(prefix = "pe.ask.persistence")
public record PostgresqlConnectionProperties(
        String dbDriver,
        String primary,
        Pool pool,
        Validation validation,
        Map<String, Database> databases
) {

    public PostgresqlConnectionProperties {
        dbDriver = hasText(dbDriver)
                ? dbDriver
                : "postgresql";

        primary = hasText(primary)
                ? primary
                : "primary";

        pool = pool == null
                ? new Pool(null, null, null, null)
                : pool;

        validation = validation == null
                ? new Validation(null, null)
                : validation;

        databases = databases == null
                ? Map.of()
                : Map.copyOf(databases);
    }

    public record Database(
            Boolean enabled,
            String host,
            Integer port,
            String database,
            String schema,
            String username,
            String password,
            Pool pool
    ) {

        public boolean isEnabled() {
            return enabled == null || enabled;
        }
    }

    public record Pool(
            Integer initialSize,
            Integer maxSize,
            Duration maxIdleTime,
            String validationQuery
    ) {
    }

    public record Validation(
            Boolean enabled,
            Duration timeout
    ) {

        public Validation {
            enabled = enabled == null
                    ? Boolean.TRUE
                    : enabled;

            timeout = timeout == null
                    ? Duration.ofSeconds(10)
                    : timeout;
        }

        public boolean isEnabled() {
            return Boolean.TRUE.equals(enabled);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
