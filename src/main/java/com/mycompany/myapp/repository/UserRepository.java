package com.mycompany.myapp.repository;

import com.mycompany.myapp.domain.User;

import org.joda.time.DateTime;
import org.springframework.data.repository.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the User entity.
 */
public interface UserRepository extends Repository<User, String> {

    Optional<User> findOneByActivationKey(String activationKey);

    List<User> findAllByActivatedIsFalseAndCreatedDateBefore(DateTime dateTime);

    Optional<User> findOneByEmail(String email);

    Optional<User> findOneByLogin(String login);

    Optional<User> save(User t);

    void delete(User t);

}
