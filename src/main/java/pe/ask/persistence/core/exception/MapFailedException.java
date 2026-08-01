package pe.ask.persistence.core.exception;


import pe.ask.exception.core.annotation.ExceptionDef;
import pe.ask.exception.core.model.BaseException;

/**
 * Exception thrown when mapping between a domain object and an entity fails.
 * <p>
 * This exception is mapped to the {@link ErrorCatalog#MAP_FAILED} error definition.
 * </p>
 *
 * @author Allan Sagastegui
 */
@ExceptionDef(ErrorCatalog.class)
public class MapFailedException extends BaseException {

    /**
     * Default constructor for MapFailedException.
     */
    public MapFailedException() {
        super();
    }

    /**
     * Returns a builder for MapFailedException.
     *
     * @return a new builder instance
     */
    public static Builder<MapFailedException> builder() {
        return new Builder<>(MapFailedException.class);
    }
}