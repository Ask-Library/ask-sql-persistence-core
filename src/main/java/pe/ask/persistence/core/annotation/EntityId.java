package pe.ask.persistence.core.annotation;

import org.springframework.data.annotation.Id;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field, getter, or record component as the primary key
 * of a persistence entity.
 *
 * <p>This annotation is a composed annotation based on Spring Data's
 * {@link Id} annotation.</p>
 *
 * <p>The annotated property should be of type {@link java.util.UUID}.</p>
 *
 * @author Allan Sagastegui
 */
@Id
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.RECORD_COMPONENT
})
public @interface EntityId {
}