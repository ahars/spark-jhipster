package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.Author;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Author entity.
 */
public interface AuthorRepository extends JpaRepository<Author, Long> {

}
