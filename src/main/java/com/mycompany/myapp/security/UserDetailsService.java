package com.mycompany.myapp.security;

import com.mycompany.myapp.domain.Authority;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.List;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(UserDetailsService.class);

    @Inject
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating {}", login);
        String lowercaseLogin = login.toLowerCase();
        Optional<User> userFromDatabase =  userRepository.findOneByLogin(lowercaseLogin);
        boolean activated =
            userFromDatabase.map(u -> u.getActivated())
            .orElseThrow(() -> new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database"));

        if (!activated) {
            throw new UserNotActivatedException("User " + lowercaseLogin + " was not activated");
        }

        List<GrantedAuthority> grantedAuthorities = userFromDatabase.map(u -> u.getAuthorities().stream().map(authority -> {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority.getName());
                return grantedAuthority ;
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            return new org.springframework.security.core.userdetails.User(lowercaseLogin,
                userFromDatabase.map(u -> u.getPassword()).orElse(null),
                grantedAuthorities);
    }
}
