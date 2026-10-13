package com.crunchycode.bookstore.dto;

/**
 * DTO for Author responses.
 * Strict separation: entities are transformed into this object before returning.
 */
public record AuthorResponse(
        Long id,
        String name,
        String email,
        String bio,
        int bookCount
) {}
