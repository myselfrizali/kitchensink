package org.quickstarts.kitchensink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.quickstarts.kitchensink.model.User;
import org.quickstarts.kitchensink.pojo.ApiError;
import org.quickstarts.kitchensink.pojo.FieldError;
import org.quickstarts.kitchensink.repository.UserRepository;
import org.quickstarts.kitchensink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    private static ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeAll
    static void beforeAll() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRegisterSuccess() throws Exception {
        // Arrange
        String email = "test@example.com";
        String password = "Pass@123";

        User userByEmail = null;  // Simulate email not found in the database
        when(userRepository.findByEmail(email)).thenReturn(userByEmail);

        // Act & Assert
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string("{\"status\":201,\"message\":\"User successfully created\"}"));

        verify(userRepository).findByEmail(email);
        verify(userService).createUser(any(User.class));  // Verifying that the user service was called
    }

    @Test
    void testRegisterEmailConflict() throws Exception {
        // Arrange
        String email = "test@example.com";
        String password = "Pass@123";

        User userByEmail = new User();  // Simulate email already exists
        when(userRepository.findByEmail(email)).thenReturn(userByEmail);

        // Act & Assert
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"status\":400,\"error\":\"Bad Request\",\"message\":\"User with email test@example.com already exists.\",\"path\":\"/users/register\"}"));

        verify(userRepository).findByEmail(email);
        verify(userService, times(0)).createUser(any(User.class));  // Ensure user creation wasn't attempted
    }

    @ParameterizedTest
    @CsvSource({"john32432, Invalid email", "john32432@cac, Invalid email", "john32432@cac., Invalid email"})
    void testRegisterUser_invalidEmail(String email, String expected) throws Exception {
        String password = "Pass@123";
        String requestBody = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(1);
        assertThat(apiError.getDetails().getFirst().getMessage()).isEqualTo(expected);
    }

    @Test
    void testRegisterUser_emailIsNull() throws Exception {
        String password = "Pass@123";
        String requestBody = "{\"email\": null, \"password\":\"" + password + "\"}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(1);
        List<String> messages = apiError.getDetails().stream()
                .map(FieldError::getMessage)
                .toList();
        assertThat(messages).contains("Email is mandatory");
    }

    @Test
    void testRegisterUser_emailFieldIsMissing() throws Exception {
        String password = "Pass@123";
        String requestBody = "{\"password\":\"" + password + "\"}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(1);
        List<String> messages = apiError.getDetails().stream()
                .map(FieldError::getMessage)
                .toList();
        assertThat(messages).contains("Email is mandatory");
    }

    @ParameterizedTest
    @ValueSource(strings = {"pass@123", "Pass@", "Pass123", "Pass@12", "Pass<>123", "{}Pass123"})
    void testRegisterUser_invalidPassword(String password) throws Exception {
        String email = "john@doe.com";
        String requestBody = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(1);
        assertThat(apiError.getDetails().getFirst().getMessage()).isEqualTo("Password must be at least 8 characters long and include uppercase, lowercase, and special characters.");
    }

    @Test
    void testRegisterUser_passwordIsNull() throws Exception {
        String email = "john@doe.com";
        String requestBody = "{\"email\":\"" + email + "\", \"password\": null}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(1);
        List<String> messages = apiError.getDetails().stream()
                .map(FieldError::getMessage)
                .toList();
        assertThat(messages).contains("Password is mandatory");
    }

    @Test
    void testRegisterUser_passwordFieldMissing() throws Exception {
        String email = "john@doe.com";
        String requestBody = "{\"email\":\"" + email + "\"}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(1);
        List<String> messages = apiError.getDetails().stream()
                .map(FieldError::getMessage)
                .toList();
        assertThat(messages).contains("Password is mandatory");
    }
}
