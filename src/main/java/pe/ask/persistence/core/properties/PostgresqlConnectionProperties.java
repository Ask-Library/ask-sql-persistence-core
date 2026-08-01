package pe.ask.persistence.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

/**
 * Configuration properties for multiple PostgreSQL database connections.
 *
 * @param dbDriver driver identifier
 * @param primary primary database name
 * @param pool default pool configuration
 * @param databases configured databases
 *
 * @author Allan Sagastegui
 */
@ConfigurationProperties(prefix = "pe.ask.persistence")
public record PostgresqlConnectionProperties(
        String dbDriver,
        String primary,
        Pool pool,
        Map<String, Database> databases
) {

    public PostgresqlConnectionProperties {
        dbDriver = hasText(dbDriver) ? dbDriver : "postgresql";
        primary = hasText(primary) ? primary : "primary";
        pool = pool == null ? new Pool(null, null, null, null) : pool;
        databases = databases == null ? Map.of() : Map.copyOf(databases);
    }

    /**
     * PostgreSQL database configuration.
     */
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

    /**
     * R2DBC connection pool configuration.
     */
    public record Pool(
            Integer initialSize,
            Integer maxSize,
            Duration maxIdleTime,
            String validationQuery
    ) {
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
