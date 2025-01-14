package org.quickstarts.kitchensink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.quickstarts.kitchensink.pojo.ApiError;
import org.quickstarts.kitchensink.pojo.FieldError;
import org.quickstarts.kitchensink.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    private static ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @BeforeAll
    static void beforeAll() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testAuthenticateSuccess() throws Exception {
        // Arrange
        String username = "john@doe.com";
        String token = "access_token";
        String refreshToken = "refresh_token";

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jwtTokenService.generateToken(username)).thenReturn(token);
        when(jwtTokenService.generateRefreshToken(username)).thenReturn(refreshToken);

        // Act & Assert
        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\", \"password\":\"Pass@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(token))
                .andExpect(jsonPath("$.refresh_token").value(refreshToken));

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenService).generateToken(username);
        verify(jwtTokenService).generateRefreshToken(username);
    }

    @Test
    void testAuthenticateFailure() throws Exception {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john@doe.com\", \"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Bad Credentials"));

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"john32432", "john32432@cac", "john32432@cac."})
    void testAuthToken_invalidUsername(String username) throws Exception {
        String password = "Pass@123";
        String requestBody = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/auth/token")
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
        assertThat(apiError.getDetails().getFirst().getMessage()).isEqualTo("Invalid username");
    }

    @Test
    void testAuthToken_usernameIsNull() throws Exception {
        String password = "Pass@123";
        String requestBody = "{\"username\": null, \"password\":\"" + password + "\"}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/auth/token")
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
        assertThat(messages).contains("Username is mandatory");
    }

    @Test
    void testAuthToken_usernameFieldIsMissing() throws Exception {
        String password = "Pass@123";
        String requestBody = "{\"password\":\"" + password + "\"}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/auth/token")
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
        assertThat(messages).contains("Username is mandatory");
    }

    @Test
    void testAuthToken_passwordIsNull() throws Exception {
        String username = "john@doe.com";
        String requestBody = "{\"username\":\"" + username + "\", \"password\": null}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/auth/token")
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
    void testAuthToken_passwordFieldMissing() throws Exception {
        String username = "john@doe.com";
        String requestBody = "{\"username\":\"" + username + "\"}";

        MvcResult mvcResult = mockMvc.perform(
                        post("/auth/token")
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
