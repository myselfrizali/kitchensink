package org.quickstarts.kitchensink.controller;

import lombok.RequiredArgsConstructor;
import org.quickstarts.kitchensink.pojo.AuthRequestDTO;
import org.quickstarts.kitchensink.pojo.AuthResponseDTO;
import org.quickstarts.kitchensink.service.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    @RequestMapping(value = "/token", method = RequestMethod.POST)
    public ResponseEntity<?> authenticate(@RequestBody AuthRequestDTO authRequestDTO) {
        try {
            Authentication authenticate =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword()));
            if (authenticate.isAuthenticated()) {
                String token = jwtTokenService.generateToken(authRequestDTO.getUsername());
                String refreshToken = jwtTokenService.generateRefreshToken(authRequestDTO.getUsername());
                return ResponseEntity.ok(new AuthResponseDTO(token, refreshToken));
            }

        } catch (BadCredentialsException e) {
            logger.error("Authentication failed: Bad credentials for user {}", authRequestDTO.getUsername());
        }  catch (Exception e) {
            logger.error("Authentication error: {}", e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad Credentials");
    }
}
