package pe.ask.persistence.core.autoconfig;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.r2dbc.autoconfigure.R2dbcAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import pe.ask.persistence.core.connection.PostgresqlConnectionsRegistry;
import pe.ask.persistence.core.properties.PostgresqlConnectionProperties;

/**
 * Auto-configures all PostgreSQL R2DBC connections declared under
 * {@code pe.ask.persistence.databases}.
 *
 * @author Allan Sagastegui
 */
@AutoConfiguration(before = R2dbcAutoConfiguration.class)
@ConditionalOnClass({
        ConnectionPool.class,
        PostgresqlConnectionFactory.class,
        DatabaseClient.class,
        R2dbcEntityTemplate.class
})
@ConditionalOnProperty(
        prefix = "pe.ask.persistence",
        name = "db-driver",
        havingValue = "postgresql",
        matchIfMissing = true
)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE )
@EnableConfigurationProperties(PostgresqlConnectionProperties.class)
@Import(PostgresqlConnectionsRegistry.class)
public class PostgreSQLConnectionPoolAutoConfiguration {
}