package org.quickstarts.kitchensink.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService();
    }

    @Test
    void testGenerateToken_withUsername() {
        String username = "testuser";
        String token = jwtTokenService.generateToken(username);

        assertThat(token).isNotNull();
        assertThat(token).startsWith("ey");
    }

    @Test
    void testGenerateToken_withExtraClaims() {
        String username = "testuser";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "admin");

        String token = jwtTokenService.generateToken(username, extraClaims);

        assertThat(token).isNotNull();
        assertThat(token).startsWith("ey");

        Claims claims = Jwts.parser()
                .verifyWith(jwtTokenService.getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
//                .setSigningKey(jwtTokenService.getKey())
//                .parseClaimsJws(token)
//                .getBody();

        assertThat(claims).containsEntry("role", "admin");
    }

    @Test
    void testGenerateRefreshToken_withUsername() {
        String username = "testuser";
        String refreshToken = jwtTokenService.generateRefreshToken(username);

        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).startsWith("ey");

        String tokenType = Jwts.parser()
                .verifyWith(jwtTokenService.getKey())
                .build()
                .parseSignedClaims(refreshToken)
//                .setSigningKey(jwtTokenService.getKey())
//                .parseClaimsJws(refreshToken)
                .getHeader()
                .get("typ")
                .toString();

        assertThat(tokenType).isEqualTo("refresh");
    }

    @Test
    void testValidateToken_validToken() {
        String username = "testuser";
        String token = jwtTokenService.generateToken(username);

        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")  // Password won't be used in this validation
                .authorities("ROLE_USER")
                .build();

        boolean isValid = jwtTokenService.validateToken(token, userDetails);

        assertThat(isValid).isTrue();
    }

//    @Test
//    void testValidateToken_expiredToken() throws InterruptedException {
//        String username = "testuser";
//        String token = jwtTokenService.generateToken(username);
//
//        // Simulate a delay to make the token expire
//        Thread.sleep(2000);
//
//        UserDetails userDetails = User.builder()
//                .username(username)
//                .password("password")
//                .authorities("ROLE_USER")
//                .build();
//
//        boolean isValid = jwtTokenService.validateToken(token, userDetails);
//
//        assertThat(isValid).isFalse();
//    }

    @Test
    void testExtractClaims() {
        String username = "testuser";
        String token = jwtTokenService.generateToken(username);

        // Extract username from token
        String extractedUsername = jwtTokenService.extractUsername(token);
        assertThat(extractedUsername).isEqualTo(username);

        // Extract expiration date from token
        Date expiration = jwtTokenService.extractExpiration(token);
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new Date());
    }

//    @Test
//    void testIsTokenExpired() throws InterruptedException {
//        String username = "testuser";
//        String token = jwtTokenService.generateToken(username);
//
//        // Simulate a delay to make the token expire
//        Thread.sleep(2000);
//
//        boolean isExpired = jwtTokenService.isTokenExpired(token);
//
//        assertThat(isExpired).isTrue();
//    }

    @Test
    void testIsRefreshToken() {
        String username = "testuser";
        String refreshToken = jwtTokenService.generateRefreshToken(username);

        boolean isRefresh = jwtTokenService.isRefreshToken(refreshToken);

        assertThat(isRefresh).isTrue();
    }

    @Test
    void testValidateToken_withMockUserDetails() {
        UserDetails mockUserDetails = Mockito.mock(UserDetails.class);
        Mockito.when(mockUserDetails.getUsername()).thenReturn("testuser");

        String token = jwtTokenService.generateToken("testuser");

        boolean isValid = jwtTokenService.validateToken(token, mockUserDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void testGenerateToken_withMockKey() {
        // Mock Key generation
        SecretKey mockKey = Mockito.mock(SecretKey.class);
        JwtTokenService mockJwtTokenService = Mockito.spy(new JwtTokenService());
        Mockito.doReturn(mockKey).when(mockJwtTokenService).getKey();

        String token = mockJwtTokenService.generateToken("testuser");

        assertThat(token).isNotNull();
        assertThat(token).startsWith("ey");
    }

}