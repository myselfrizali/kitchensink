package org.quickstarts.kitchensink.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quickstarts.kitchensink.exception.MemberAlreadyExistsException;
import org.quickstarts.kitchensink.model.User;
import org.quickstarts.kitchensink.pojo.ApiResponse;
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
    public ResponseEntity<ApiResponse<String>> register(@RequestBody UserSignUpDTO userSignUpDTO) {
        log.info("Creating user: {}", userSignUpDTO);
        User userByEmail = userRepository.findByEmail(userSignUpDTO.getEmail());
        if (userByEmail != null) {
            throw new MemberAlreadyExistsException(("User with email " + userSignUpDTO.getEmail() + " already exists."));
        }

        String encodedPassword = passwordEncoder.encode(userSignUpDTO.getPassword());

        User newUser = new User();
        newUser.setEmail(userSignUpDTO.getEmail());
        newUser.setPassword(encodedPassword);

        userService.createUser(newUser);

        HttpStatus status = HttpStatus.CREATED;
        ApiResponse<String> response = new ApiResponse<>(
                status.value(),
                "User successfully created",
                null
        );

        return new ResponseEntity<>(response, status);
    }
}
