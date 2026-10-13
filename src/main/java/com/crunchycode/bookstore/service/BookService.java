package com.crunchycode.bookstore.service;

import com.crunchycode.bookstore.dto.BookCreateRequest;
import com.crunchycode.bookstore.dto.BookResponse;
import com.crunchycode.bookstore.dto.BookUpdateRequest;
import com.crunchycode.bookstore.exception.DuplicateResourceException;
import com.crunchycode.bookstore.exception.ResourceNotFoundException;
import com.crunchycode.bookstore.model.Author;
import com.crunchycode.bookstore.model.Book;
import com.crunchycode.bookstore.repository.AuthorRepository;
import com.crunchycode.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service encapsulating business logic for Books.
 * Strictly uses constructor injection.
 * Validates relationship integrity (author exists) and unique constraints (ISBN).
 */
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    // Strict constructor injection - no @Autowired on fields
    public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository);
        this.authorRepository = Objects.requireNonNull(authorRepository);
    }

    public List<BookResponse> getAllBooks(Long authorId) {
        List<Book> books = (authorId != null)
                ? bookRepository.findByAuthorId(authorId)
                : bookRepository.findAll();

        return books.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return mapToResponse(book);
    }

    public BookResponse createBook(BookCreateRequest request) {
        // Enforce referential integrity: Author must exist
        Author author = authorRepository.findById(request.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + request.authorId()));

        // Enforce business uniqueness: ISBN must be unique
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateResourceException("Book already exists with ISBN: " + request.isbn());
        }

        Book book = new Book();
        book.setTitle(request.title().trim());
        book.setIsbn(request.isbn().trim().toUpperCase());
        book.setPrice(request.price());
        book.setPublicationYear(request.publicationYear());
        book.setAuthorId(author.getId());

        Book saved = bookRepository.save(book);
        return mapToResponse(saved, author.getName());
    }

    public BookResponse updateBook(Long id, BookUpdateRequest request) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        // Enforce referential integrity: Target Author must exist
        Author author = authorRepository.findById(request.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + request.authorId()));

        // If ISBN changed, enforce uniqueness
        if (!existing.getIsbn().equalsIgnoreCase(request.isbn())
                && bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateResourceException("Book already exists with ISBN: " + request.isbn());
        }

        existing.setTitle(request.title().trim());
        existing.setIsbn(request.isbn().trim().toUpperCase());
        existing.setPrice(request.price());
        existing.setPublicationYear(request.publicationYear());
        existing.setAuthorId(author.getId());

        Book saved = bookRepository.save(existing);
        return mapToResponse(saved, author.getName());
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    private BookResponse mapToResponse(Book book) {
        String authorName = authorRepository.findById(book.getAuthorId())
                .map(Author::getName)
                .orElse("Unknown Author");
        return mapToResponse(book, authorName);
    }

    private BookResponse mapToResponse(Book book, String authorName) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getPrice(),
                book.getPublicationYear(),
                book.getAuthorId(),
                authorName
        );
    }
}
