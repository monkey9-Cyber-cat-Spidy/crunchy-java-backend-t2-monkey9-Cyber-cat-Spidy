package com.crunchycode.bookstore;

import com.crunchycode.bookstore.dto.AuthorCreateRequest;
import com.crunchycode.bookstore.dto.BookCreateRequest;
import com.crunchycode.bookstore.repository.AuthorRepository;
import com.crunchycode.bookstore.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookstoreApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setup() {
        bookRepository.clear();
        authorRepository.clear();
    }

    @Test
    void testAuthorCrudAndSemantics() throws Exception {
        // 1. Create Author -> 201 Created with Location header
        AuthorCreateRequest createReq = new AuthorCreateRequest("J.K. Rowling", "jk@example.com", "British author");
        String authorResContent = mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/authors/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("J.K. Rowling"))
                .andExpect(jsonPath("$.email").value("jk@example.com"))
                .andExpect(jsonPath("$.bookCount").value(0))
                .andReturn().getResponse().getContentAsString();

        Long authorId = objectMapper.readTree(authorResContent).get("id").asLong();

        // 2. Fetch Author by ID -> 200 OK
        mockMvc.perform(get("/api/v1/authors/" + authorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(authorId))
                .andExpect(jsonPath("$.name").value("J.K. Rowling"));

        // 3. Validation failure on Create -> 400 Bad Request
        AuthorCreateRequest invalidReq = new AuthorCreateRequest("", "invalid-email", "Bio");
        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists());

        // 4. Duplicate Author Email -> 409 Conflict
        mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isConflict());

        // 5. Delete Author -> 204 No Content
        mockMvc.perform(delete("/api/v1/authors/" + authorId))
                .andExpect(status().isNoContent());

        // 6. Fetch deleted Author -> 404 Not Found
        mockMvc.perform(get("/api/v1/authors/" + authorId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testBookCrudAndRelationshipSemantics() throws Exception {
        // 1. Create Author first
        AuthorCreateRequest authorReq = new AuthorCreateRequest("George Orwell", "orwell@example.com", "English novelist");
        String authorJson = mockMvc.perform(post("/api/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long authorId = objectMapper.readTree(authorJson).get("id").asLong();

        // 2. Create Book for Author -> 201 Created with Location
        BookCreateRequest bookReq = new BookCreateRequest("1984", "9780451524935", new BigDecimal("14.99"), 1949, authorId);
        String bookJson = mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookReq)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/books/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("1984"))
                .andExpect(jsonPath("$.authorName").value("George Orwell"))
                .andReturn().getResponse().getContentAsString();

        Long bookId = objectMapper.readTree(bookJson).get("id").asLong();

        // 3. Prevent deleting Author while books exist -> 400 Bad Request
        mockMvc.perform(delete("/api/v1/authors/" + authorId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("still have 1 registered book")));

        // 4. Validation Failure on Book -> 400 Bad Request
        BookCreateRequest invalidBook = new BookCreateRequest("", "bad-isbn", new BigDecimal("-5.00"), 3000, -1L);
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title").exists())
                .andExpect(jsonPath("$.validationErrors.isbn").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists())
                .andExpect(jsonPath("$.validationErrors.publicationYear").exists());

        // 5. Delete Book -> 204 No Content
        mockMvc.perform(delete("/api/v1/books/" + bookId))
                .andExpect(status().isNoContent());

        // 6. Delete Author now succeeds -> 204 No Content
        mockMvc.perform(delete("/api/v1/authors/" + authorId))
                .andExpect(status().isNoContent());
    }
}
