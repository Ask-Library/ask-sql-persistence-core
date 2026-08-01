package pe.ask.persistence.core.mapper;

import org.springframework.beans.BeanUtils;
import pe.ask.persistence.core.exception.MapFailedException;

/**
 * A generic mapper for converting between domain models and database entities.
 * <p>
 * Uses reflection and {@link org.springframework.beans.BeanUtils} to copy properties
 * between the domain and entity objects.
 * </p>
 *
 * @param <D> the domain model type
 * @param <E> the entity model type
 *
 * @author Allan Sagastegui
 */
public class GenericBeanMapper<D, E> {

    private final Class<D> domainClass;
    private final Class<E> entityClass;

    /**
     * Constructs a new GenericBeanMapper.
     *
     * @param domainClass the class of the domain model
     * @param entityClass the class of the entity model
     */
    public GenericBeanMapper(Class<D> domainClass, Class<E> entityClass) {
        this.domainClass = domainClass;
        this.entityClass = entityClass;
    }

    /**
     * Converts a domain model to an entity.
     *
     * @param domain the domain model to convert
     * @return the resulting entity
     * @throws MapFailedException if mapping fails
     */
    public E toEntity(D domain) {
        try {
            E entity = entityClass.getDeclaredConstructor().newInstance();
            if (domain != null) {
                BeanUtils.copyProperties(domain, entity);
            }
            return entity;
        } catch (Exception e) {
            throw new MapFailedException();
        }
    }

    /**
     * Converts an entity to a domain model.
     *
     * @param entity the entity to convert
     * @return the resulting domain model
     * @throws MapFailedException if mapping fails
     */
    public D toDomain(E entity) {
        try {
            D domain = domainClass.getDeclaredConstructor().newInstance();
            if (entity != null) {
                BeanUtils.copyProperties(entity, domain);
            }
            return domain;
        } catch (Exception e) {
            throw new MapFailedException();
        }
    }
}