package com.auth.AuthService.repository;

import com.auth.AuthService.model.Role;
import com.auth.AuthService.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void savesUserWithRoleAndFindsByUsername() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded");
        user.setEmail("alice@example.com");
        user.setRole(Role.ADMIN);

        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("alice");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
        assertThat(found.get().getUsername()).isEqualTo("alice");
        assertThat(found.get().getEmail()).isEqualTo("alice@example.com");
        assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void detectsDuplicateUsernameAndEmail() {
        User first = new User();
        first.setUsername("bob");
        first.setPassword("encoded");
        first.setRole(Role.USER);
        userRepository.save(first);

        assertThat(userRepository.existsByUsername("bob")).isTrue();
        assertThat(userRepository.existsByUsername("nobody")).isFalse();

        User second = new User();
        second.setUsername("carol");
        second.setPassword("encoded");
        second.setEmail("carol@example.com");
        second.setRole(Role.USER);
        userRepository.save(second);

        assertThat(userRepository.existsByEmail("carol@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("unknown@example.com")).isFalse();
    }

}