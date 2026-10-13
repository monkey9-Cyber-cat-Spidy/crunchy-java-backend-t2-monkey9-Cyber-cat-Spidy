package com.crunchycode.bookstore.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Domain entity representing a Book.
 * Tied to an Author via authorId.
 * Stored only in Repository memory — never exposed directly through controllers.
 */
public class Book {
    private Long id;
    private String title;
    private String isbn;
    private BigDecimal price;
    private Integer publicationYear;
    private Long authorId;

    public Book() {
    }

    public Book(Long id, String title, String isbn, BigDecimal price, Integer publicationYear, Long authorId) {
        this.id = id;
        this.title = title;
        this.isbn = isbn;
        this.price = price;
        this.publicationYear = publicationYear;
        this.authorId = authorId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
