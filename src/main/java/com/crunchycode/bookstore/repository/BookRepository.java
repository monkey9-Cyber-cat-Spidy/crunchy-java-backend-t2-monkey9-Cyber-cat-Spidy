package com.crunchycode.bookstore.repository;

import com.crunchycode.bookstore.model.Book;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory repository for Books.
 * Strictly decoupled from database/JPA; uses thread-safe ConcurrentHashMap.
 */
@Repository
public class BookRepository {

    private final Map<Long, Book> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    public Book save(Book book) {
        if (book.getId() == null) {
            book.setId(idSequence.incrementAndGet());
        }
        storage.put(book.getId(), book);
        return book;
    }

    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Book> findAll() {
        return new ArrayList<>(storage.values());
    }

    public List<Book> findByAuthorId(Long authorId) {
        return storage.values().stream()
                .filter(b -> Objects.equals(b.getAuthorId(), authorId))
                .collect(Collectors.toList());
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    public boolean existsByIsbn(String isbn) {
        return storage.values().stream()
                .anyMatch(b -> b.getIsbn().equalsIgnoreCase(isbn));
    }

    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }

    public int countByAuthorId(Long authorId) {
        return (int) storage.values().stream()
                .filter(b -> Objects.equals(b.getAuthorId(), authorId))
                .count();
    }

    public void clear() {
        storage.clear();
        idSequence.set(0);
    }
}
