package org.quickstarts.kitchensink.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quickstarts.kitchensink.pojo.AuthRequestDTO;
import org.quickstarts.kitchensink.service.JwtTokenService;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testAuthenticateSuccess() throws Exception {
        // Arrange
        String username = "user1";
        String password = "password123";
        String token = "access_token";
        String refreshToken = "refresh_token";

        AuthRequestDTO authRequestDTO = new AuthRequestDTO(username, password);
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jwtTokenService.generateToken(username)).thenReturn(token);
        when(jwtTokenService.generateRefreshToken(username)).thenReturn(refreshToken);

        // Act & Assert
        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user1\", \"password\":\"password123\"}"))
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
        String username = "user1";
        String password = "password123";
        AuthRequestDTO authRequestDTO = new AuthRequestDTO(username, password);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user1\", \"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Bad Credentials"));

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
