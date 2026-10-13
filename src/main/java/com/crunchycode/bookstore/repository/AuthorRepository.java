package com.crunchycode.bookstore.repository;

import com.crunchycode.bookstore.model.Author;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory repository for Authors.
 * Strictly decoupled from database/JPA; uses thread-safe ConcurrentHashMap.
 */
@Repository
public class AuthorRepository {

    private final Map<Long, Author> storage = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(0);

    public Author save(Author author) {
        if (author.getId() == null) {
            author.setId(idSequence.incrementAndGet());
        }
        storage.put(author.getId(), author);
        return author;
    }

    public Optional<Author> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Author> findAll() {
        return new ArrayList<>(storage.values());
    }

    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    public boolean existsByEmail(String email) {
        return storage.values().stream()
                .anyMatch(a -> a.getEmail().equalsIgnoreCase(email));
    }

    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }

    public void clear() {
        storage.clear();
        idSequence.set(0);
    }
}
