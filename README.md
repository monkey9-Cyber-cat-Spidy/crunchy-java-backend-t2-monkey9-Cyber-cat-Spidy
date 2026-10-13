# Crunchy Code Internship — Task 2: Layered Spring Boot REST API

**Candidate Handle**: `monkey9-Cyber-cat-Spidy`  
**Repository**: `crunchy-java-backend-t2-monkey9-Cyber-cat-Spidy`  
**Framework**: Spring Boot 3.2.5 (`Java 21 LTS`)  
**Storage**: In-Memory Thread-Safe Repositories (`ConcurrentHashMap`)  
**Postman Collection**: [postman/bookstore-api-collection.json](postman/bookstore-api-collection.json)

---

## 1. Architectural Highlights

- **Strict Three-Layer Separation**:
  - **Controller Layer** (`AuthorController`, `BookController`): Exclusively handles routing, extracting HTTP parameters, and translating outputs into standard HTTP status codes (`201`, `200`, `204`, `Location` headers). **Zero business logic.**
  - **Service Layer** (`AuthorService`, `BookService`): Enforces domain rules, referential integrity (e.g. verifying an author exists before creating a book, prohibiting author deletion when books exist), and entity-to-DTO conversion.
  - **Repository Layer** (`AuthorRepository`, `BookRepository`): Thread-safe in-memory storage using `ConcurrentHashMap` and atomic ID sequences.
- **DTOs and Encapsulation**: Entities never leave the service layer. Requests use dedicated record DTOs (`AuthorCreateRequest`, `BookCreateRequest`), and responses are returned via `AuthorResponse` and `BookResponse`.
- **Constructor Injection**: Exclusively constructor-based dependency injection. **No `@Autowired` on fields.**
- **Bean Validation**: Input requests validated via `@NotBlank`, `@Email`, `@Size`, `@DecimalMin`, `@Digits`, `@Min`, `@Max`, and `@Pattern`.
- **Global Error Handling**: `GlobalExceptionHandler` converts domain exceptions and validation errors into uniform `ApiErrorResponse` payloads with HTTP `400`, `404`, `409`, or `500`.

---

## 2. API Endpoints Table

### Authors (`/api/v1/authors`)

| HTTP Method | Endpoint | Request Body | Success Status | Error Codes | Description |
| :--- | :--- | :--- | :---: | :---: | :--- |
| `GET` | `/api/v1/authors` | None | `200 OK` | `500` | Fetch all authors with book counts |
| `GET` | `/api/v1/authors/{id}` | None | `200 OK` | `404` | Fetch a single author by ID |
| `POST` | `/api/v1/authors` | `AuthorCreateRequest` | `201 Created` (`Location`) | `400`, `409` | Create a new author |
| `PUT` | `/api/v1/authors/{id}` | `AuthorUpdateRequest` | `200 OK` | `400`, `404`, `409` | Update author details |
| `DELETE` | `/api/v1/authors/{id}` | None | `204 No Content` | `400`, `404` | Delete author (rejected if books exist) |

### Books (`/api/v1/books`)

| HTTP Method | Endpoint | Request Body | Success Status | Error Codes | Description |
| :--- | :--- | :--- | :---: | :---: | :--- |
| `GET` | `/api/v1/books` | None | `200 OK` | `500` | Fetch all books (supports `?authorId=`) |
| `GET` | `/api/v1/books/{id}` | None | `200 OK` | `404` | Fetch a single book by ID |
| `POST` | `/api/v1/books` | `BookCreateRequest` | `201 Created` (`Location`) | `400`, `404`, `409` | Create book linked to an author |
| `PUT` | `/api/v1/books/{id}` | `BookUpdateRequest` | `200 OK` | `400`, `404`, `409` | Update book details |
| `DELETE` | `/api/v1/books/{id}` | None | `204 No Content` | `404` | Delete a book by ID |

---

## 3. HTTP Semantics & Failure Cases Covered

| Scenario | HTTP Status | Behavior |
| :--- | :---: | :--- |
| **Successful Creation** | `201 Created` | Returns created DTO body and `Location: /api/v1/{resource}/{id}` header |
| **Successful Deletion** | `204 No Content` | Returns an empty response body |
| **Missing Resource** | `404 Not Found` | Returns structured JSON explaining the missing resource ID |
| **Bean Validation Failure** | `400 Bad Request` | Returns map of field-specific errors (e.g. `title: Book title is required`) |
| **Business Rule Violation** | `400 Bad Request` | E.g. Attempting to delete an author who still has active books in the catalog |
| **Duplicate Conflict** | `409 Conflict` | E.g. Registering an author with an existing email or a book with an existing ISBN |

---

## 4. How to Run & Test

### Run Unit & Integration Tests:
```powershell
mvn clean test
```

### Run the Application:
```powershell
mvn spring-boot:run
```
The server will start on port `8080`.

### Postman Testing:
Import [postman/bookstore-api-collection.json](postman/bookstore-api-collection.json) into Postman or Bruno. The collection includes 17 pre-configured requests covering all CRUD endpoints and positive/negative scenarios.
