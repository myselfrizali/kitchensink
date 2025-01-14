package org.quickstarts.kitchensink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quickstarts.kitchensink.model.User;
import org.quickstarts.kitchensink.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_userFound() {
        // Arrange
        String username = "test@example.com";
        User user = new User(username, "encodedPassword");
        when(userRepo.findByEmail(username)).thenReturn(user);

        // Act
        UserDetails userDetails = userServiceImpl.loadUserByUsername(username);

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(username);
        verify(userRepo, times(1)).findByEmail(username);
    }

    @Test
    void testLoadUserByUsername_userNotFound() {
        // Arrange
        String username = "test@example.com";
        when(userRepo.findByEmail(username)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> userServiceImpl.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email: " + username);

        verify(userRepo, times(1)).findByEmail(username);
    }

    @Test
    void testSaveOrUpdate() {
        // Arrange
        User user = new User("test@example.com", "plainPassword");

        // Act
        userServiceImpl.saveOrUpdate(user);

        // Assert
        verify(userRepo, times(1)).save(user);  // The user should be saved to the repository
    }
}
