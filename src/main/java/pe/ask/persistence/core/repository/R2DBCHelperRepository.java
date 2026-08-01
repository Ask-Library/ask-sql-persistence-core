package pe.ask.persistence.core.repository;

import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

/**
 * A helper repository that combines the functionality of {@link ReactiveCrudRepository} and
 * {@link ReactiveQueryByExampleExecutor}.
 *
 * @param <E> the domain type the repository manages
 */
public interface R2DBCHelperRepository<E> extends ReactiveCrudRepository<E, java.util.UUID>, ReactiveQueryByExampleExecutor<E> {
}