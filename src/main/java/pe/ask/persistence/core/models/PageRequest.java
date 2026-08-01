package pe.ask.persistence.core.models;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a request for paginated data.
 *
 * <p>Contains the page number, page size, sort field, and sort direction.
 *
 * @author Allan Sagastegui
 */
@Data
@AllArgsConstructor
public class PageRequest {

  /** Default constructor for PageRequest. */
  public PageRequest() {}

  /**
   * The page number to request.
   *
   * @param page The page number.
   * @return The current page number.
   */
  private int page = 0;

  /**
   * The size of the page to request.
   *
   * @param size The page size.
   * @return The current page size.
   */
  private int size = 10;

  /**
   * The field to sort by.
   *
   * @param sort The sort field.
   * @return The current sort field.
   */
  private String sort = "id";

  /**
   * The direction to sort by.
   *
   * @param direction The sort direction.
   * @return The current sort direction.
   */
  private String direction = "ASC";

  /**
   * Creates a new builder for {@link PageRequest}.
   *
   * @return A new {@link PageRequestBuilder}.
   */
  public static PageRequestBuilder builder() {
    return new PageRequestBuilder();
  }

  /**
   * Creates a builder from the current instance.
   *
   * @return A new {@link PageRequestBuilder} initialized with the values of this instance.
   */
  public PageRequestBuilder toBuilder() {
    return new PageRequestBuilder()
        .page(this.page)
        .size(this.size)
        .sort(this.sort)
        .direction(this.direction);
  }

  /**
   * Used by Lombok to implement `equals`.
   *
   * @param other The other object.
   * @return true if the other object is of the same class.
   */
  protected boolean canEqual(final Object other) {
    return other instanceof PageRequest;
  }

  /** A builder for {@link PageRequest}. */
  public static class PageRequestBuilder {
    private int page;
    private int size;
    private String sort;
    private String direction;

    PageRequestBuilder() {}

    /**
     * Sets the page number.
     *
     * @param page The page number.
     * @return This builder.
     */
    public PageRequestBuilder page(final int page) {
      this.page = page;
      return this;
    }

    /**
     * Sets the page size.
     *
     * @param size The page size.
     * @return This builder.
     */
    public PageRequestBuilder size(final int size) {
      this.size = size;
      return this;
    }

    /**
     * Sets the sort field.
     *
     * @param sort The sort field.
     * @return This builder.
     */
    public PageRequestBuilder sort(final String sort) {
      this.sort = sort;
      return this;
    }

    /**
     * Sets the sort direction.
     *
     * @param direction The sort direction.
     * @return This builder.
     */
    public PageRequestBuilder direction(final String direction) {
      this.direction = direction;
      return this;
    }

    /**
     * Builds the {@link PageRequest}.
     *
     * @return A new {@link PageRequest} instance.
     */
    public PageRequest build() {
      return new PageRequest(this.page, this.size, this.sort, this.direction);
    }

    /**
     * Returns a string representation of the builder.
     *
     * @return a string representation of the builder
     */
    @Override
    public String toString() {
      return "PageRequest.PageRequestBuilder(page="
          + this.page
          + ", size="
          + this.size
          + ", sort="
          + this.sort
          + ", direction="
          + this.direction
          + ")";
    }
  }
}