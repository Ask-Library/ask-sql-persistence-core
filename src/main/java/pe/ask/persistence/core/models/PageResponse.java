package pe.ask.persistence.core.models;

import java.util.List;
import lombok.Data;

/**
 * Represents a paginated response containing a list of items and pagination metadata.
 *
 * @param <T> the type of the content items in the page
 * @author Allan Sagastegui
 */
@Data
public class PageResponse<T> {

  /**
   * The list of items in the page.
   *
   * @param content The list of items.
   * @return The list of items.
   */
  private List<T> content;

  /**
   * The current page number.
   *
   * @param pageNumber The page number.
   * @return The page number.
   */
  private int pageNumber;

  /**
   * The size of the page.
   *
   * @param pageSize The page size.
   * @return The page size.
   */
  private int pageSize;

  /**
   * Whether this is the last page.
   *
   * @param isLast True if it's the last page.
   * @return True if it's the last page.
   */
  private boolean isLast;

  /**
   * Whether there is a previous page.
   *
   * @param hasPrevious True if there is a previous page.
   * @return True if there is a previous page.
   */
  private boolean hasPrevious;

  /**
   * Whether there is a next page.
   *
   * @param hasNext True if there is a next page.
   * @return True if there is a next page.
   */
  private boolean hasNext;

  /** Default constructor. */
  public PageResponse() {}

  /**
   * All arguments constructor.
   *
   * @param content The list of items.
   * @param pageNumber The page number.
   * @param pageSize The page size.
   * @param isLast True if it's the last page.
   * @param hasPrevious True if there is a previous page.
   * @param hasNext True if there is a next page.
   */
  public PageResponse(
      List<T> content,
      int pageNumber,
      int pageSize,
      boolean isLast,
      boolean hasPrevious,
      boolean hasNext) {
    this.content = content;
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.isLast = isLast;
    this.hasPrevious = hasPrevious;
    this.hasNext = hasNext;
  }

  /**
   * Creates a new builder for {@link PageResponse}.
   *
   * @param <T> the type of the content items in the page
   * @return A new {@link PageResponseBuilder}.
   */
  public static <T> PageResponseBuilder<T> builder() {
    return new PageResponseBuilder<>();
  }

  /**
   * A builder for {@link PageResponse}.
   *
   * @param <T> the type of the content items in the page
   */
  public static class PageResponseBuilder<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private boolean isLast;
    private boolean hasPrevious;
    private boolean hasNext;

    PageResponseBuilder() {}

    /**
     * Sets the content.
     *
     * @param content The list of items.
     * @return This builder.
     */
    public PageResponseBuilder<T> content(final List<T> content) {
      this.content = content;
      return this;
    }

    /**
     * Sets the page number.
     *
     * @param pageNumber The page number.
     * @return This builder.
     */
    public PageResponseBuilder<T> pageNumber(final int pageNumber) {
      this.pageNumber = pageNumber;
      return this;
    }

    /**
     * Sets the page size.
     *
     * @param pageSize The page size.
     * @return This builder.
     */
    public PageResponseBuilder<T> pageSize(final int pageSize) {
      this.pageSize = pageSize;
      return this;
    }

    /**
     * Sets whether this is the last page.
     *
     * @param isLast True if it's the last page.
     * @return This builder.
     */
    public PageResponseBuilder<T> isLast(final boolean isLast) {
      this.isLast = isLast;
      return this;
    }

    /**
     * Sets whether there is a previous page.
     *
     * @param hasPrevious True if there is a previous page.
     * @return This builder.
     */
    public PageResponseBuilder<T> hasPrevious(final boolean hasPrevious) {
      this.hasPrevious = hasPrevious;
      return this;
    }

    /**
     * Sets whether there is a next page.
     *
     * @param hasNext True if there is a next page.
     * @return This builder.
     */
    public PageResponseBuilder<T> hasNext(final boolean hasNext) {
      this.hasNext = hasNext;
      return this;
    }

    /**
     * Builds the {@link PageResponse}.
     *
     * @return A new {@link PageResponse} instance.
     */
    public PageResponse<T> build() {
      return new PageResponse<>(
          this.content,
          this.pageNumber,
          this.pageSize,
          this.isLast,
          this.hasPrevious,
          this.hasNext);
    }

    /**
     * Returns a string representation of the builder.
     *
     * @return a string representation of the builder
     */
    @Override
    public String toString() {
      return "PageResponse.PageResponseBuilder(content="
          + this.content
          + ", pageNumber="
          + this.pageNumber
          + ", pageSize="
          + this.pageSize
          + ", isLast="
          + this.isLast
          + ", hasPrevious="
          + this.hasPrevious
          + ", hasNext="
          + this.hasNext
          + ")";
    }
  }

  /**
   * Used by Lombok to implement `equals`.
   *
   * @param other The other object.
   * @return true if the other object is of the same class.
   */
  protected boolean canEqual(final Object other) {
    return other instanceof PageResponse;
  }
}