package com.crunchycode.bookstore.service;

import com.crunchycode.bookstore.dto.AuthorCreateRequest;
import com.crunchycode.bookstore.dto.AuthorResponse;
import com.crunchycode.bookstore.dto.AuthorUpdateRequest;
import com.crunchycode.bookstore.exception.BusinessRuleException;
import com.crunchycode.bookstore.exception.DuplicateResourceException;
import com.crunchycode.bookstore.exception.ResourceNotFoundException;
import com.crunchycode.bookstore.model.Author;
import com.crunchycode.bookstore.repository.AuthorRepository;
import com.crunchycode.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service encapsulating business logic for Authors.
 * Strictly uses constructor injection.
 * Entities are processed here and mapped to DTOs before reaching the controller.
 */
@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    // Strict constructor injection - no @Autowired on fields
    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = Objects.requireNonNull(authorRepository);
        this.bookRepository = Objects.requireNonNull(bookRepository);
    }

    public List<AuthorResponse> getAllAuthors() {
        return authorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AuthorResponse getAuthorById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
        return mapToResponse(author);
    }

    public AuthorResponse createAuthor(AuthorCreateRequest request) {
        if (authorRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Author already exists with email: " + request.email());
        }

        Author author = new Author();
        author.setName(request.name().trim());
        author.setEmail(request.email().trim().toLowerCase());
        author.setBio(request.bio() != null ? request.bio().trim() : null);

        Author saved = authorRepository.save(author);
        return mapToResponse(saved);
    }

    public AuthorResponse updateAuthor(Long id, AuthorUpdateRequest request) {
        Author existing = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));

        // If email is changing, make sure it is not taken by another author
        if (!existing.getEmail().equalsIgnoreCase(request.email())
                && authorRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Author already exists with email: " + request.email());
        }

        existing.setName(request.name().trim());
        existing.setEmail(request.email().trim().toLowerCase());
        existing.setBio(request.bio() != null ? request.bio().trim() : null);

        Author saved = authorRepository.save(existing);
        return mapToResponse(saved);
    }

    public void deleteAuthor(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author not found with id: " + id);
        }

        // Business rule: Cannot delete an author who still has active books in catalog
        int bookCount = bookRepository.countByAuthorId(id);
        if (bookCount > 0) {
            throw new BusinessRuleException("Cannot delete author with id " + id + " because they still have " + bookCount + " registered book(s)");
        }

        authorRepository.deleteById(id);
    }

    private AuthorResponse mapToResponse(Author author) {
        int books = bookRepository.countByAuthorId(author.getId());
        return new AuthorResponse(
                author.getId(),
                author.getName(),
                author.getEmail(),
                author.getBio(),
                books
        );
    }
}
