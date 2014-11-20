package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Book entity.
 */
public interface BookRepository extends JpaRepository<Book, Long> {

}
