package com.crunchycode.bookstore.dto;

import java.math.BigDecimal;

/**
 * DTO for Book responses.
 * Contains both authorId and authorName for convenience, shielding the internal entity representation.
 */
public record BookResponse(
        Long id,
        String title,
        String isbn,
        BigDecimal price,
        Integer publicationYear,
        Long authorId,
        String authorName
) {}
