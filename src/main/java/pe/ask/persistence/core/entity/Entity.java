package pe.ask.persistence.core.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;

/**
 * Abstract base class for all entities.
 *
 * <p>Provides common fields for versioning and timestamps.
 */
@Getter
@Setter
public abstract class Entity {

  /**
   * The version of the entity for optimistic locking.
   *
   * @param version The new version number.
   * @return The current version number.
   */
  @Version
  private Long version;

  /**
   * The timestamp when the entity was created.
   *
   * @param createdAt The creation timestamp.
   * @return The creation timestamp.
   */
  @CreatedDate
  private LocalDateTime createdAt;

  /**
   * The timestamp when the entity was last updated.
   *
   * @param updatedAt The last update timestamp.
   * @return The last update timestamp.
   */
  @LastModifiedDate
  private LocalDateTime updatedAt;

  /**
   * A transient flag to indicate if the entity is new.
   *
   * @param isNew The new status.
   * @return True if the entity is new, false otherwise.
   */
  @Transient
  private boolean isNew = true;

  /** Default constructor for the entity. */
  protected Entity() {
    // for JPA
  }
}