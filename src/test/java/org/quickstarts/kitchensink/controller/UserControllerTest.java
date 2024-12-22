package org.quickstarts.kitchensink.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quickstarts.kitchensink.model.User;
import org.quickstarts.kitchensink.pojo.UserSignUpDTO;
import org.quickstarts.kitchensink.repository.UserRepository;
import org.quickstarts.kitchensink.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void testRegisterSuccess() throws Exception {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        UserSignUpDTO userSignUpDTO = new UserSignUpDTO(email, password);

        User userByEmail = null;  // Simulate email not found in the database
        when(userRepository.findByEmail(email)).thenReturn(userByEmail);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        // Act & Assert
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string("User successfully created"));

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userService).createUser(any(User.class));  // Verifying that the user service was called
    }

    @Test
    void testRegisterEmailConflict() throws Exception {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        UserSignUpDTO userSignUpDTO = new UserSignUpDTO(email, password);

        User userByEmail = new User();  // Simulate email already exists
        when(userRepository.findByEmail(email)).thenReturn(userByEmail);

        // Act & Assert
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"password\":\"password123\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email already exists"));

        verify(userRepository).findByEmail(email);
        verify(userService, times(0)).createUser(any(User.class));  // Ensure user creation wasn't attempted
    }
}
