package org.quickstarts.kitchensink.controller;

import org.junit.jupiter.api.Test;
import org.quickstarts.kitchensink.model.User;
import org.quickstarts.kitchensink.pojo.UserSignUpDTO;
import org.quickstarts.kitchensink.repository.UserRepository;
import org.quickstarts.kitchensink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testRegisterSuccess() throws Exception {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        UserSignUpDTO userSignUpDTO = new UserSignUpDTO(email, password);

        User userByEmail = null;  // Simulate email not found in the database
        when(userRepository.findByEmail(email)).thenReturn(userByEmail);

        // Act & Assert
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string("{\"status\":201,\"message\":\"User successfully created\"}"));

        verify(userRepository).findByEmail(email);
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
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"status\":400,\"error\":\"Bad Request\",\"message\":\"User with email test@example.com already exists.\",\"path\":\"/users/register\"}"));

        verify(userRepository).findByEmail(email);
        verify(userService, times(0)).createUser(any(User.class));  // Ensure user creation wasn't attempted
    }
}
