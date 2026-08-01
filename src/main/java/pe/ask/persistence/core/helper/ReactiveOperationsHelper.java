package pe.ask.persistence.core.helper;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import pe.ask.core.mapper.EntityMapper;
import pe.ask.core.mapper.impl.GenericBeanMapper;
import pe.ask.core.model.pagination.PageResponse;
import pe.ask.persistence.core.entity.Entity;
import pe.ask.persistence.core.repository.R2DBCHelperRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.FeatureDescriptor;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Helper base class for reactive operations, providing generic CRUD functionality.
 * <p>
 * Bridges the gap between domain models and database entities using a generic bean mapper.
 * It includes methods for saving, finding, patching, and paginating records.
 * </p>
 *
 * @param <D> the domain model type
 * @param <E> the entity model type
 * @param <R> the reactive repository type extending {@link R2DBCHelperRepository}
 *
 * @author Allan Sagastegui
 */
public abstract class ReactiveOperationsHelper <
        D,
        E extends Entity,
        R extends R2DBCHelperRepository<E>
        > implements ApplicationContextAware, InitializingBean {

    /**
     * The repository used for database operations.
     */
    protected R repository;

    private final EntityMapper<D, E> mapper;

    private ApplicationContext applicationContext;

    /**
     * The name of the identifier field in the entity.
     */
    protected String idFieldName;

    /**
     * Constructs a new ReactiveOperationsHelper.
     *
     * @param repository the repository instance
     */
    @SuppressWarnings("unchecked")
    protected ReactiveOperationsHelper(R repository) {
        Class<?>[] typeArguments = GenericTypeResolver.resolveTypeArguments(getClass(), ReactiveOperationsHelper.class);
        Class<D> domainClass = (Class<D>) Objects.requireNonNull(typeArguments)[0];
        Class<E> entityClass = (Class<E>) Objects.requireNonNull(typeArguments)[1];
        this.mapper = new GenericBeanMapper<>(domainClass, entityClass);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() {
        Class<?>[] typeArguments = GenericTypeResolver.resolveTypeArguments(getClass(), ReactiveOperationsHelper.class);
        Class<R> repositoryClass = (Class<R>) Objects.requireNonNull(typeArguments)[2];
        this.repository = applicationContext.getBean(repositoryClass);
    }

    /**
     * Gets the current repository instance.
     *
     * @return the repository instance
     */
    protected R getRepository() {
        return this.repository;
    }

    /**
     * Converts a domain model to an entity.
     *
     * @param domain the domain model
     * @return the corresponding entity
     */
    protected E toEntity(D domain) {
        return mapper.toEntity(domain);
    }

    /**
     * Converts an entity to a domain model.
     *
     * @param domain the entity
     * @return the corresponding domain model
     */
    protected D toDomain(E domain) {
        return mapper.toDomain(domain);
    }

    /**
     * Saves a domain model.
     *
     * @param domain the domain model to save
     * @return a Mono emitting the saved domain model
     */
    public Mono<D> save(D domain) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .map(this::beforeSave)
                .flatMap(repository::save)
                .map(this::toDomain);
    }

    /**
     * Saves multiple domain models.
     *
     * @param domains a Flux of domain models to save
     * @return a Flux emitting the saved domain models
     */
    public Flux<D> saveAll(Flux<D> domains) {
        return repository.saveAll(domains.map(this::toEntity))
                .map(this::toDomain);
    }

    /**
     * Finds a domain model by its identifier.
     *
     * @param id the identifier
     * @return a Mono emitting the domain model if found
     */
    public Mono<D> findById(UUID id) {
        return repository.findById(id)
                .map(this::toDomain);
    }

    /**
     * Checks if a domain model exists by its identifier.
     *
     * @param id the identifier
     * @return a Mono emitting true if it exists, false otherwise
     */
    public Mono<Boolean> existsById(UUID id) {
        return repository.existsById(id);
    }

    /**
     * Counts the total number of domain models.
     *
     * @return a Mono emitting the total count
     */
    public Mono<Long> count() {
        return repository.count();
    }

    /**
     * Finds domain models matching the given example.
     *
     * @param domain the example domain model
     * @return a Flux emitting the matching domain models
     */
    public Flux<D> findByExample(D domain) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .map(Example::of)
                .flatMapMany(repository::findAll)
                .map(this::toDomain);
    }

    /**
     * Finds all domain models.
     *
     * @return a Flux emitting all domain models
     */
    public Flux<D> findAll() {
        return repository.findAll()
                .map(this::toDomain);
    }

    /**
     * Finds a paginated list of domain models matching the given example with sorting.
     *
     * @param domain the example domain model
     * @param page the page number
     * @param size the page size
     * @param sort the sort field
     * @param direction the sort direction
     * @return a Mono emitting the paginated response
     */
    public Mono<PageResponse<D>> findPaginated(
            D domain,
            int page,
            int size,
            String sort,
            String direction
    ) {
        Sort.Direction sortDirection = Optional.ofNullable(direction)
                .filter("asc"::equalsIgnoreCase)
                .map(value -> Sort.Direction.ASC)
                .orElse(Sort.Direction.DESC);

        String sortProperty = Optional.ofNullable(sort)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElse(this.idFieldName);

        org.springframework.data.domain.PageRequest pageRequest =
                org.springframework.data.domain.PageRequest.of(
                        page,
                        size,
                        Sort.by(sortDirection, sortProperty)
                );

        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .map(entity -> Example.of(
                        entity,
                        ExampleMatcher.matching()
                                .withIgnoreNullValues()
                                .withStringMatcher(
                                        ExampleMatcher.StringMatcher.CONTAINING
                                )
                                .withIgnoreCase()
                ))
                .flatMap(example ->
                        repository.findBy(
                                example,
                                query -> query.page(pageRequest)
                        )
                )
                .map(result -> PageResponse.<D>builder()
                        .content(
                                result.getContent()
                                        .stream()
                                        .map(this::toDomain)
                                        .toList()
                        )
                        .pageNumber(result.getNumber())
                        .pageSize(result.getSize())
                        .totalElements(result.getTotalElements())
                        .totalPages((long) result.getTotalPages())
                        .isLast(result.isLast())
                        .hasPrevious(result.hasPrevious())
                        .hasNext(result.hasNext())
                        .build()
                );
    }

    /**
     * Finds a paginated list of domain models matching the given example.
     *
     * @param domain the example domain model
     * @param page the page number
     * @param size the page size
     * @return a Mono emitting the paginated response
     */
    public Mono<PageResponse<D>> findPaginated(D domain, int page, int size) {
        return this.findPaginated(domain, page, size, null, null);
    }

    /**
     * Finds a paginated list of all domain models.
     *
     * @param page the page number
     * @param size the page size
     * @return a Mono emitting the paginated response
     */
    public Mono<PageResponse<D>> findPaginated(int page, int size) {
        return this.findPaginated(null, page, size, null, null);
    }

    /**
     * Patches a domain model by its identifier with partial data.
     *
     * @param id the identifier
     * @param partialDomain the partial domain model data
     * @return a Mono emitting the patched domain model
     */
    public Mono<D> patch(UUID id, D partialDomain) {
        return repository.findById(id)
                .map(existingEntity -> {
                    E incomingEntity = this.toEntity(partialDomain);
                    copyNonNullProperties(incomingEntity, existingEntity);
                    return existingEntity;
                })
                .flatMap(repository::save)
                .map(this::toDomain);
    }

    /**
     * Counts the number of domain models matching the given example.
     *
     * @param domain the example domain model
     * @return a Mono emitting the count
     */
    public Mono<Long> countByExample(D domain) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .map(Example::of)
                .flatMap(repository::count);
    }

    /**
     * Checks if a domain model matching the given example exists.
     *
     * @param domain the example domain model
     * @return a Mono emitting true if it exists, false otherwise
     */
    public Mono<Boolean> existsByExample(D domain) {
        return Mono.justOrEmpty(domain)
                .map(this::toEntity)
                .map(Example::of)
                .flatMap(repository::exists);
    }

    /**
     * Hook method called before saving an entity.
     *
     * @param entity the entity to be saved
     * @return the modified entity
     */
    protected E beforeSave(E entity) {
        return entity;
    }

    /**
     * Copies non-null properties from source to target.
     *
     * @param source the source object
     * @param target the target object
     */
    protected void copyNonNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);

        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }
}
