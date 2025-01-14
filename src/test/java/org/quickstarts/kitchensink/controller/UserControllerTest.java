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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    private static ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        verify(userService).saveOrUpdate(any(User.class));  // Verifying that the user service was called
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
        verify(userService, times(0)).saveOrUpdate(any(User.class));  // Ensure user creation wasn't attempted
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

    @Test
    void testUpdatePassword_success() throws Exception {
        // Arrange
        String body = """
                {
                    "email": "test@example.com",
                    "existingPassword": "Pass@123",
                    "password": "Pass@1234",
                    "confirmPassword": "Pass@1234"
                }
                """;
        String existingPassword = passwordEncoder.encode("Pass@123");
        User userByEmail = mock(User.class);
        when(userRepository.findByEmail("test@example.com")).thenReturn(userByEmail);
        when(userByEmail.getPassword()).thenReturn(existingPassword);
        doNothing().when(userByEmail).setPassword(any(String.class));
        doNothing().when(userService).saveOrUpdate(any(User.class));

        // Act & Assert
        mockMvc.perform(put("/users/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"status\":200,\"message\":\"Password successfully updated\"}"));

        verify(userRepository).findByEmail("test@example.com");
        verify(userByEmail).setPassword(any(String.class));
        verify(userService).saveOrUpdate(any(User.class));
    }

    @Test
    void testUpdatePassword_throwExceptionIfEmailNotFound() throws Exception {
        // Arrange
        String body = """
                {
                    "email": "test@example.com",
                    "existingPassword": "Pass@123",
                    "password": "Pass@1234",
                    "confirmPassword": "Pass@1234"
                }
                """;
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(put("/users/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andReturn();

        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(404);
        assertThat(apiError.getError()).isEqualTo("Not Found");
        assertThat(apiError.getMessage()).isEqualTo("User with email test@example.com does not exist.");

        verify(userRepository).findByEmail("test@example.com");
        verify(userService, never()).saveOrUpdate(any(User.class));
    }

    @Test
    void testUpdatePassword_throwExceptionIfCurrentPasswordNotMatch() throws Exception {
        // Arrange
        String body = """
                {
                    "email": "test@example.com",
                    "existingPassword": "Pass@123",
                    "password": "Pass@1234",
                    "confirmPassword": "Pass@1234"
                }
                """;
        String existingPassword = passwordEncoder.encode("Password@123");
        User userByEmail = mock(User.class);
        when(userRepository.findByEmail("test@example.com")).thenReturn(userByEmail);
        when(userByEmail.getPassword()).thenReturn(existingPassword);

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(put("/users/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Existing password does not match the current password.");

        verify(userRepository).findByEmail("test@example.com");
        verify(userService, never()).saveOrUpdate(any(User.class));
        verify(userByEmail).getPassword();
        verify(userByEmail, never()).setPassword(any(String.class));
    }

    @Test
    void testUpdatePassword_throwExceptionIfCurrentAndNewPasswordMatches() throws Exception {
        // Arrange
        String body = """
                {
                    "email": "test@example.com",
                    "existingPassword": "Pass@123",
                    "password": "Pass@123",
                    "confirmPassword": "Pass@123"
                }
                """;
        String existingPassword = passwordEncoder.encode("Pass@123");
        User userByEmail = mock(User.class);
        when(userRepository.findByEmail("test@example.com")).thenReturn(userByEmail);
        when(userByEmail.getPassword()).thenReturn(existingPassword);

        // Act & Assert
        MvcResult mvcResult = mockMvc.perform(put("/users/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("New password can not be the same.");

        verify(userRepository).findByEmail("test@example.com");
        verify(userService, never()).saveOrUpdate(any(User.class));
        verify(userByEmail).getPassword();
        verify(userByEmail, never()).setPassword(any(String.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"john32432", "john32432@cac", "john32432@cac."})
    void testUpdatePassword_invalidEmail(String email) throws Exception {
        String requestBody = "{" +
                "\"email\": \"" + email + "\"," +
                "\"existingPassword\": \"Pass@123\"," +
                "\"password\": \"Pass@1234\"," +
                "\"confirmPassword\": \"Pass@1234\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(
                        put("/users/update-password")
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
        assertThat(apiError.getDetails().getFirst().getMessage()).isEqualTo("Invalid email");
    }

    @Test
    void testUpdatePassword_emailIsNull() throws Exception {
        String requestBody = "{" +
                "\"email\": null," +
                "\"existingPassword\": \"Pass@123\"," +
                "\"password\": \"Pass@1234\"," +
                "\"confirmPassword\": \"Pass@1234\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(
                        put("/users/update-password")
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
    void testUpdatePassword_emailFieldIsMissing() throws Exception {
        String requestBody = "{" +
                "\"existingPassword\": \"Pass@123\"," +
                "\"password\": \"Pass@1234\"," +
                "\"confirmPassword\": \"Pass@1234\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(
                        put("/users/update-password")
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
    void testUpdatePassword_invalidPassword(String password) throws Exception {
        String requestBody = "{" +
                "\"email\": \"john@doe.com\"," +
                "\"existingPassword\": \"Pass@123\"," +
                "\"password\": \"" + password + "\"," +
                "\"confirmPassword\": \"" + password + "\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(
                        put("/users/update-password")
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
    void testUpdatePassword_passwordIsNull() throws Exception {
        String requestBody = "{" +
                "\"email\": \"john@doe.com\"," +
                "\"existingPassword\": \"Pass@123\"," +
                "\"password\": null," +
                "\"confirmPassword\": \"Pass@1234\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(
                        put("/users/update-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(2);
        List<String> messages = apiError.getDetails().stream()
                .map(FieldError::getMessage)
                .toList();
        assertThat(messages).containsExactlyInAnyOrder("Password is mandatory", "Password and Confirm Password must match");
    }

    @Test
    void testUpdatePassword_passwordFieldMissing() throws Exception {
        String requestBody = "{" +
                "\"email\": \"john@doe.com\"," +
                "\"existingPassword\": \"Pass@123\"," +
                "\"confirmPassword\": \"Pass@1234\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(
                        put("/users/update-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(2);
        List<String> messages = apiError.getDetails().stream()
                .map(FieldError::getMessage)
                .toList();
        assertThat(messages).containsExactlyInAnyOrder("Password is mandatory", "Password and Confirm Password must match");
    }

    @Test
    void testUpdatePassword_passwordAndConfirmPasswordNotMatch() throws Exception {
        String requestBody = "{" +
                "\"email\": \"john@doe.com\"," +
                "\"existingPassword\": \"Pass@123\"," +
                "\"password\": \"Pass@12345\"," +
                "\"confirmPassword\": \"Pass@1234\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(
                        put("/users/update-password")
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
        assertThat(apiError.getDetails().getFirst().getMessage()).isEqualTo("Password and Confirm Password must match");
    }

    @Test
    void testUpdatePassword_confirmPasswordIsNull() throws Exception {
        String requestBody = "{" +
                "\"email\": \"john@doe.com\"," +
                "\"existingPassword\": \"Pass@123\"," +
                "\"password\": \"Pass@12345\"," +
                "\"confirmPassword\": null" +
                "}";

        MvcResult mvcResult = mockMvc.perform(
                        put("/users/update-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(2);
        List<String> messages = apiError.getDetails().stream()
                .map(FieldError::getMessage)
                .toList();
        assertThat(messages).containsExactlyInAnyOrder("Password and Confirm Password must match",
                "Confirm Password must not be empty");
    }

    @Test
    void testUpdatePassword_confirmPasswordFieldIsMissing() throws Exception {
        String requestBody = "{" +
                "\"email\": \"john@doe.com\"," +
                "\"existingPassword\": \"Pass@123\"," +
                "\"password\": \"Pass@12345\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(
                        put("/users/update-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiError apiError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiError.class);
        assertThat(apiError.getStatus()).isEqualTo(400);
        assertThat(apiError.getError()).isEqualTo("Bad Request");
        assertThat(apiError.getMessage()).isEqualTo("Validation failed");
        assertThat(apiError.getDetails()).hasSize(2);
        List<String> messages = apiError.getDetails().stream()
                .map(FieldError::getMessage)
                .toList();
        assertThat(messages).containsExactlyInAnyOrder("Password and Confirm Password must match",
                "Confirm Password must not be empty");
    }

    @Test
    void testUpdatePassword_existingPasswordIsEmpty() throws Exception {
        String requestBody = "{" +
                "\"email\": \"john@doe.com\"," +
                "\"existingPassword\": \"\"," +
                "\"password\": \"Pass@1234\"," +
                "\"confirmPassword\": \"Pass@1234\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(
                        put("/users/update-password")
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
        assertThat(apiError.getDetails().getFirst().getMessage()).isEqualTo("Existing password is mandatory");
    }

    @Test
    void testUpdatePassword_existingPasswordIsNull() throws Exception {
        String requestBody = "{" +
                "\"email\": \"john@doe.com\"," +
                "\"existingPassword\": null," +
                "\"password\": \"Pass@1234\"," +
                "\"confirmPassword\": \"Pass@1234\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(
                        put("/users/update-password")
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
        assertThat(apiError.getDetails().getFirst().getMessage()).isEqualTo("Existing password is mandatory");
    }

    @Test
    void testUpdatePassword_existingPasswordFieldIsMissing() throws Exception {
        String requestBody = "{" +
                "\"email\": \"john@doe.com\"," +
                "\"password\": \"Pass@1234\"," +
                "\"confirmPassword\": \"Pass@1234\"" +
                "}";

        MvcResult mvcResult = mockMvc.perform(
                        put("/users/update-password")
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
        assertThat(apiError.getDetails().getFirst().getMessage()).isEqualTo("Existing password is mandatory");
    }
}
