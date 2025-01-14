package org.quickstarts.kitchensink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quickstarts.kitchensink.exception.InvalidPasswordException;
import org.quickstarts.kitchensink.exception.MemberAlreadyExistsException;
import org.quickstarts.kitchensink.exception.MemberNotFoundException;
import org.quickstarts.kitchensink.model.User;
import org.quickstarts.kitchensink.pojo.ApiResponse;
import org.quickstarts.kitchensink.pojo.UserPasswordDTO;
import org.quickstarts.kitchensink.pojo.UserSignUpDTO;
import org.quickstarts.kitchensink.repository.UserRepository;
import org.quickstarts.kitchensink.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<ApiResponse<String>> register(@RequestBody @Valid UserSignUpDTO userSignUpDTO) {
        log.info("Creating user: {}", userSignUpDTO);
        User userByEmail = userRepository.findByEmail(userSignUpDTO.getEmail());
        if (userByEmail != null) {
            throw new MemberAlreadyExistsException(("User with email " + userSignUpDTO.getEmail() + " already exists."));
        }

        String encodedPassword = passwordEncoder.encode(userSignUpDTO.getPassword());

        User newUser = new User();
        newUser.setEmail(userSignUpDTO.getEmail());
        newUser.setPassword(encodedPassword);

        userService.saveOrUpdate(newUser);

        HttpStatus status = HttpStatus.CREATED;
        ApiResponse<String> response = new ApiResponse<>(
                status.value(),
                "User successfully created",
                null
        );

        return new ResponseEntity<>(response, status);
    }

    @RequestMapping(value = "/update-password", method = RequestMethod.PUT)
    public ResponseEntity<ApiResponse<String>> updatePassword(@RequestBody @Valid UserPasswordDTO userPasswordDTO) {
        String email = userPasswordDTO.getEmail();
        log.info("Updating password for user: {}", email);
        User userByEmail = userRepository.findByEmail(email);
        if (userByEmail == null) {
            throw new MemberNotFoundException(("User with email " + email + " does not exist."));
        }

        String currentEncodedPassword = userByEmail.getPassword();
        if (!passwordEncoder.matches(userPasswordDTO.getExistingPassword(), currentEncodedPassword)) {
            throw new InvalidPasswordException("Existing password does not match the current password.");
        }

        if (passwordEncoder.matches(userPasswordDTO.getPassword(), currentEncodedPassword)) {
            throw new InvalidPasswordException("New password can not be the same.");
        }

        String encodedNewPassword = passwordEncoder.encode(userPasswordDTO.getPassword());
        userByEmail.setPassword(encodedNewPassword);
        userService.saveOrUpdate(userByEmail);

        HttpStatus status = HttpStatus.OK;
        ApiResponse<String> response = new ApiResponse<>(
                status.value(),
                "Password successfully updated",
                null
        );

        return new ResponseEntity<>(response, status);
    }
}
