package org.quickstarts.kitchensink.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quickstarts.kitchensink.model.User;
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
    public ResponseEntity<String> register(@RequestBody UserSignUpDTO userSignUpDTO) {
        log.info("Registering user: {}", userSignUpDTO);
        User userByEmail = userRepository.findByEmail(userSignUpDTO.getEmail());
        if (userByEmail != null) {
            return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
        }

        String encodedPassword = passwordEncoder.encode(userSignUpDTO.getPassword());

        User newUser = new User();
        newUser.setEmail(userSignUpDTO.getEmail());
        newUser.setPassword(encodedPassword);

        userService.createUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully created");
    }
}
