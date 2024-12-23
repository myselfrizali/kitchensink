package org.quickstarts.kitchensink.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quickstarts.kitchensink.pojo.AuthRequestDTO;
import org.quickstarts.kitchensink.pojo.AuthResponseDTO;
import org.quickstarts.kitchensink.service.JwtTokenService;
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

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

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
            log.error("Authentication failed: Bad credentials for user {}", authRequestDTO.getUsername());
        }  catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad Credentials");
    }
}
