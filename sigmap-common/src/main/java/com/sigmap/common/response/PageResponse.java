package com.sigmap.common.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic response class for paginated data. This class wraps paginated data with metadata about
 * the page, such as page number, size, total elements, and navigation information.
 *
 * @param <T> The type of elements in the page
 */
@Data
@Builder
public class PageResponse<T> {
    /** Current page number (0-based). */
    private Long page;

    /** Number of elements per page. */
    private Long size;

    /** Total number of pages. */
    private Long totalPages;

    /** Total number of elements across all pages. */
    private Long total;

    /** Whether this is the first page. */
    private Boolean first;

    /** Whether this is the last page. */
    private Boolean last;

    /** Whether the page is empty (contains no elements). */
    private Boolean empty;

    /** The actual content of the current page. */
    private List<T> content;

    /**
     * Creates a PageResponse instance from a Spring Page object. This static factory method converts
     * Spring's Page implementation to our custom PageResponse format.
     *
     * @param <T> The type of elements in the page
     * @param page The Spring Page object to convert
     * @return A new PageResponse instance containing the page data
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .page(Long.valueOf(page.getNumber()))
                .size(Long.valueOf(page.getSize()))
                .totalPages(Long.valueOf(page.getTotalPages()))
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .total(page.getTotalElements())
                .content(page.getContent())
                .build();
    }

    /**
     * Creates a PageResponse instance from a list of elements. This static factory method converts a
     * list of elements to our custom PageResponse format.
     *
     * @param <T> The type of elements in the page
     * @param data The list of elements to convert
     * @return A new PageResponse instance containing the page data
     */
    public static <T> PageResponse<T> of(List<T> data) {
        return PageResponse.<T>builder()
                .page(1L)
                .size(Long.valueOf(data.size()))
                .totalPages(1L)
                .first(true)
                .last(true)
                .empty(data.isEmpty())
                .total(Long.valueOf(data.size()))
                .content(data)
                .build();
    }
}

