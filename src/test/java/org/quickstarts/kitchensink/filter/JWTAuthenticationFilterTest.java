package org.quickstarts.kitchensink.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quickstarts.kitchensink.service.JwtTokenService;
import org.quickstarts.kitchensink.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JWTAuthenticationFilterTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private FilterChain filterChain;

    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtAuthenticationFilter = new JWTAuthenticationFilter(userService, jwtTokenService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_withValidToken_setsAuthentication() throws Exception {
        // Arrange
        String token = "valid-jwt-token";
        String username = "user@example.com";
        UserDetails userDetails = mock(UserDetails.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtTokenService.validateToken(token, userDetails)).thenReturn(true);
        when(jwtTokenService.isRefreshToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userDetails);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_noToken_doesNothing() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_invalidToken_doesNothing() throws Exception {
        // Arrange
        String token = "invalid-jwt-token";
        String username = "user@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.loadUserByUsername(username)).thenReturn(null); // Simulating an invalid user
        when(jwtTokenService.validateToken(token, null)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_refreshToken_doesNothing() throws Exception {
        // Arrange
        String token = "refresh-token";
        String username = "user@example.com";
        UserDetails userDetails = mock(UserDetails.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenService.extractUsername(token)).thenReturn(username);
        when(userService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtTokenService.validateToken(token, userDetails)).thenReturn(true);
        when(jwtTokenService.isRefreshToken(token)).thenReturn(true); // This is a refresh token

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull(); // No authentication should be set
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
