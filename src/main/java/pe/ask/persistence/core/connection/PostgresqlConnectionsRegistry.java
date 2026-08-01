package pe.ask.persistence.core.connection;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import pe.ask.persistence.core.exception.IllegalStateException;
import pe.ask.persistence.core.properties.PostgresqlConnectionProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registers one complete Spring R2DBC infrastructure for each configured
 * PostgreSQL database.
 *
 * @author Allan Sagastegui
 */
public final class PostgresqlConnectionsRegistry
        implements ImportBeanDefinitionRegistrar,
        EnvironmentAware,
        BeanFactoryAware {

    private static final String PROPERTY_PREFIX = "pe.ask.persistence";

    private static final int DEFAULT_INITIAL_SIZE = 5;
    private static final int DEFAULT_MAX_SIZE = 15;
    private static final Duration DEFAULT_MAX_IDLE_TIME =
            Duration.ofMinutes(30);
    private static final String DEFAULT_VALIDATION_QUERY = "SELECT 1";

    private Environment environment;
    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory)
            throws BeansException {

        if (!(beanFactory instanceof ConfigurableListableBeanFactory factory)) {
            throw IllegalStateException.builder()
                    .withMessage("A ConfigurableListableBeanFactory is required")
                    .build();
        }

        this.beanFactory = factory;
    }

    @Override
    public void registerBeanDefinitions(
            @NonNull AnnotationMetadata importingClassMetadata,
            @NonNull BeanDefinitionRegistry registry
    ) {
        PostgresqlConnectionProperties properties = Binder
                .get(environment)
                .bind(
                        PROPERTY_PREFIX,
                        Bindable.of(PostgresqlConnectionProperties.class)
                )
                .orElseThrow(() ->
                        IllegalStateException.builder()
                                .withMessage(
                                        "Configuration '%s' was not found"
                                                .formatted(PROPERTY_PREFIX)
                                )
                                .build()
                );



        Map<String, PostgresqlConnectionProperties.Database>
                enabledDatabases = enabledDatabases(properties);

        validate(properties, enabledDatabases);

        enabledDatabases.forEach((databaseName, database) ->
                registerDatabaseBeans(
                        registry,
                        properties,
                        databaseName,
                        database
                )
        );
    }

    private void registerDatabaseBeans(
            BeanDefinitionRegistry registry,
            PostgresqlConnectionProperties properties,
            String databaseName,
            PostgresqlConnectionProperties.Database database
    ) {
        boolean primary = databaseName.equals(properties.primary());

        String connectionFactoryName =
                databaseName + "ConnectionFactory";
        String databaseClientName =
                databaseName + "DatabaseClient";
        String entityTemplateName =
                databaseName + "R2dbcEntityTemplate";
        String transactionManagerName =
                databaseName + "TransactionManager";
        String transactionalOperatorName =
                databaseName + "TransactionalOperator";

        registerConnectionFactory(
                registry,
                connectionFactoryName,
                databaseName,
                database,
                resolvePool(properties.pool(), database.pool()),
                primary
        );

        registerDatabaseClient(
                registry,
                databaseClientName,
                connectionFactoryName,
                primary
        );

        registerEntityTemplate(
                registry,
                entityTemplateName,
                connectionFactoryName,
                primary
        );

        registerTransactionManager(
                registry,
                transactionManagerName,
                connectionFactoryName,
                primary
        );

        registerTransactionalOperator(
                registry,
                transactionalOperatorName,
                transactionManagerName,
                primary
        );

        if (primary) {
            registerAliasIfAvailable(
                    registry,
                    connectionFactoryName,
                    "connectionFactory"
            );

            registerAliasIfAvailable(
                    registry,
                    databaseClientName,
                    "databaseClient"
            );

            registerAliasIfAvailable(
                    registry,
                    entityTemplateName,
                    "r2dbcEntityTemplate"
            );

            registerAliasIfAvailable(
                    registry,
                    transactionManagerName,
                    "transactionManager"
            );

            registerAliasIfAvailable(
                    registry,
                    transactionalOperatorName,
                    "transactionalOperator"
            );
        }
    }

    private void registerConnectionFactory(
            BeanDefinitionRegistry registry,
            String beanName,
            String databaseName,
            PostgresqlConnectionProperties.Database database,
            PoolSettings pool,
            boolean primary
    ) {
        if (beanExists(registry, beanName)) {
            return;
        }

        RootBeanDefinition definition = new RootBeanDefinition(
                ConnectionPool.class,
                () -> createConnectionPool(
                        databaseName,
                        database,
                        pool
                )
        );

        definition.setPrimary(primary);
        definition.setDestroyMethodName("dispose");

        registry.registerBeanDefinition(beanName, definition);
    }

    private void registerDatabaseClient(
            BeanDefinitionRegistry registry,
            String beanName,
            String connectionFactoryName,
            boolean primary
    ) {
        if (beanExists(registry, beanName)) {
            return;
        }

        RootBeanDefinition definition = new RootBeanDefinition(
                DatabaseClient.class,
                () -> DatabaseClient.create(
                        beanFactory.getBean(
                                connectionFactoryName,
                                ConnectionFactory.class
                        )
                )
        );

        definition.setPrimary(primary);

        registry.registerBeanDefinition(beanName, definition);
    }

    private void registerEntityTemplate(
            BeanDefinitionRegistry registry,
            String beanName,
            String connectionFactoryName,
            boolean primary
    ) {
        if (beanExists(registry, beanName)) {
            return;
        }

        RootBeanDefinition definition = new RootBeanDefinition(
                R2dbcEntityTemplate.class,
                () -> new R2dbcEntityTemplate(
                        beanFactory.getBean(
                                connectionFactoryName,
                                ConnectionFactory.class
                        )
                )
        );

        definition.setPrimary(primary);

        registry.registerBeanDefinition(beanName, definition);
    }

    private void registerTransactionManager(
            BeanDefinitionRegistry registry,
            String beanName,
            String connectionFactoryName,
            boolean primary
    ) {
        if (beanExists(registry, beanName)) {
            return;
        }

        RootBeanDefinition definition = new RootBeanDefinition(
                R2dbcTransactionManager.class,
                () -> new R2dbcTransactionManager(
                        beanFactory.getBean(
                                connectionFactoryName,
                                ConnectionFactory.class
                        )
                )
        );

        definition.setPrimary(primary);

        registry.registerBeanDefinition(beanName, definition);
    }

    private void registerTransactionalOperator(
            BeanDefinitionRegistry registry,
            String beanName,
            String transactionManagerName,
            boolean primary
    ) {
        if (beanExists(registry, beanName)) {
            return;
        }

        RootBeanDefinition definition = new RootBeanDefinition(
                TransactionalOperator.class,
                () -> TransactionalOperator.create(
                        beanFactory.getBean(
                                transactionManagerName,
                                ReactiveTransactionManager.class
                        )
                )
        );

        definition.setPrimary(primary);

        registry.registerBeanDefinition(beanName, definition);
    }

    private ConnectionPool createConnectionPool(
            String databaseName,
            PostgresqlConnectionProperties.Database database,
            PoolSettings pool
    ) {
        PostgresqlConnectionConfiguration.Builder builder =
                PostgresqlConnectionConfiguration.builder()
                        .host(required(
                                databaseName,
                                "host",
                                database.host()
                        ))
                        .port(database.port() == null
                                ? 5432
                                : database.port())
                        .database(required(
                                databaseName,
                                "database",
                                database.database()
                        ))
                        .username(required(
                                databaseName,
                                "username",
                                database.username()
                        ));

        if (hasText(database.password())) {
            builder.password(database.password());
        }

        if (hasText(database.schema())) {
            builder.schema(database.schema());
        }

        PostgresqlConnectionFactory connectionFactory =
                new PostgresqlConnectionFactory(builder.build());

        ConnectionPoolConfiguration poolConfiguration =
                ConnectionPoolConfiguration
                        .builder(connectionFactory)
                        .name(
                                "postgresql-%s-connection-pool"
                                        .formatted(databaseName)
                        )
                        .initialSize(pool.initialSize())
                        .maxSize(pool.maxSize())
                        .maxIdleTime(pool.maxIdleTime())
                        .validationQuery(pool.validationQuery())
                        .build();

        return new ConnectionPool(poolConfiguration);
    }

    private PoolSettings resolvePool(
            PostgresqlConnectionProperties.Pool global,
            PostgresqlConnectionProperties.Pool specific
    ) {
        int initialSize = firstNonNull(
                specific == null ? null : specific.initialSize(),
                global == null ? null : global.initialSize(),
                DEFAULT_INITIAL_SIZE
        );

        int maxSize = firstNonNull(
                specific == null ? null : specific.maxSize(),
                global == null ? null : global.maxSize(),
                DEFAULT_MAX_SIZE
        );

        Duration maxIdleTime = firstNonNull(
                specific == null ? null : specific.maxIdleTime(),
                global == null ? null : global.maxIdleTime(),
                DEFAULT_MAX_IDLE_TIME
        );

        String validationQuery = firstNonNull(
                specific == null ? null : specific.validationQuery(),
                global == null ? null : global.validationQuery(),
                DEFAULT_VALIDATION_QUERY
        );

        if (initialSize < 0) {
            throw IllegalStateException.builder()
                    .withMessage("Pool initial-size cannot be negative")
                    .build();
        }

        if (maxSize <= 0) {
            throw IllegalStateException.builder()
                    .withMessage("Pool max-size must be greater than zero")
                    .build();
        }

        if (initialSize > maxSize) {
            throw IllegalStateException.builder()
                    .withMessage("Pool initial-size cannot be greater than max-size")
                    .build();
        }

        return new PoolSettings(
                initialSize,
                maxSize,
                maxIdleTime,
                validationQuery
        );
    }

    private Map<String, PostgresqlConnectionProperties.Database>
    enabledDatabases(PostgresqlConnectionProperties properties) {

        Map<String, PostgresqlConnectionProperties.Database> result =
                new LinkedHashMap<>();

        properties.databases().forEach((name, database) -> {
            if (database != null && database.isEnabled()) {
                result.put(name, database);
            }
        });

        return result;
    }

    private void validate(
            PostgresqlConnectionProperties properties,
            Map<String, PostgresqlConnectionProperties.Database> databases
    ) {
        if (databases.isEmpty()) {
            throw IllegalStateException.builder()
                    .withMessage(
                            "At least one enabled database must be configured under "
                                    + "'pe.ask.persistence.databases'"
                    )
                    .build();
        }

        if (!databases.containsKey(properties.primary())) {
            throw IllegalStateException.builder()
                    .withMessage(
                            "Primary database '%s' is not configured or is disabled. Available databases: %s"
                                    .formatted(properties.primary(), databases.keySet())
                    )
                    .build();
        }
    }

    private boolean beanExists(
            BeanDefinitionRegistry registry,
            String beanName
    ) {
        return registry.containsBeanDefinition(beanName)
                || beanFactory.containsBean(beanName);
    }

    private void registerAliasIfAvailable(
            BeanDefinitionRegistry registry,
            String beanName,
            String alias
    ) {
        if (!registry.containsBeanDefinition(alias)
                && !registry.isAlias(alias)) {
            registry.registerAlias(beanName, alias);
        }
    }

    private String required(
            String databaseName,
            String property,
            String value
    ) {
        if (!hasText(value)) {
            throw IllegalStateException.builder()
                    .withMessage(
                            "Property '%s.databases.%s.%s' is required"
                                    .formatted(
                                            PROPERTY_PREFIX,
                                            databaseName,
                                            property
                                    )
                    )
                    .build();
        }

        return value;
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        throw IllegalStateException.builder()
                .withMessage("No value or default value was provided")
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record PoolSettings(
            int initialSize,
            int maxSize,
            Duration maxIdleTime,
            String validationQuery
    ) {
    }
}
