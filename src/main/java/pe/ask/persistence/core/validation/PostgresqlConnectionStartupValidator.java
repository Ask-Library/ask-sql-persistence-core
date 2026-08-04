package pe.ask.persistence.core.validation;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.ValidationDepth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import pe.ask.persistence.core.properties.PostgresqlConnectionProperties;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * Validates all configured PostgreSQL connection pools during application
 * startup.
 *
 * @author Allan Sagastegui
 */
public final class PostgresqlConnectionStartupValidator
        implements SmartInitializingSingleton {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    PostgresqlConnectionStartupValidator.class
            );

    private final ListableBeanFactory beanFactory;
    private final PostgresqlConnectionProperties properties;

    public PostgresqlConnectionStartupValidator(
            ListableBeanFactory beanFactory,
            PostgresqlConnectionProperties properties
    ) {
        this.beanFactory = beanFactory;
        this.properties = properties;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!properties.validation().isEnabled()) {
            LOGGER.warn(
                    "PostgreSQL startup connection validation is disabled"
            );
            return;
        }

        long enabledDatabaseCount = validateConfiguration();

        Map<String, ConnectionPool> connectionPools =
                beanFactory.getBeansOfType(ConnectionPool.class);

        if (connectionPools.size() < enabledDatabaseCount) {
            throw new IllegalStateException(
                    "Expected at least %d PostgreSQL connection pools, but found %d"
                            .formatted(
                                    enabledDatabaseCount,
                                    connectionPools.size()
                            )
            );
        }

        connectionPools.forEach(this::validateConnectionPool);
    }

    private long validateConfiguration() {
        if (properties.databases().isEmpty()) {
            throw new IllegalStateException(
                    "No PostgreSQL databases were configured under "
                            + "'pe.ask.persistence.databases'"
            );
        }

        PostgresqlConnectionProperties.Database primaryDatabase =
                properties.databases().get(properties.primary());

        if (primaryDatabase == null) {
            throw new IllegalStateException(
                    "Primary PostgreSQL database '%s' is not configured"
                            .formatted(properties.primary())
            );
        }

        if (!primaryDatabase.isEnabled()) {
            throw new IllegalStateException(
                    "Primary PostgreSQL database '%s' is disabled"
                            .formatted(properties.primary())
            );
        }

        long enabledDatabaseCount = properties.databases()
                .values()
                .stream()
                .filter(
                        PostgresqlConnectionProperties.Database::isEnabled
                )
                .count();

        if (enabledDatabaseCount == 0) {
            throw new IllegalStateException(
                    "At least one PostgreSQL database must be enabled"
            );
        }

        return enabledDatabaseCount;
    }

    private void validateConnectionPool(
            String beanName,
            ConnectionPool connectionPool
    ) {
        Duration timeout = properties.validation().timeout();

        try {
            Boolean valid = Mono.usingWhen(
                            Mono.from(connectionPool.create()),

                            connection -> Mono.from(
                                    connection.validate(
                                            ValidationDepth.REMOTE
                                    )
                            ),

                            connection ->
                                    Mono.from(connection.close()),

                            (connection, error) ->
                                    Mono.from(connection.close()),

                            connection ->
                                    Mono.from(connection.close())
                    )
                    .timeout(timeout)
                    .block();

            if (!Boolean.TRUE.equals(valid)) {
                throw new IllegalStateException(
                        "Remote validation returned false"
                );
            }

            LOGGER.info(
                    "PostgreSQL connection pool '{}' validated successfully",
                    beanName
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not establish a PostgreSQL connection "
                            + "for pool '%s' within %s"
                            .formatted(beanName, timeout),
                    exception
            );
        }
    }
}
