package org.quickstarts.kitchensink.controller;

import lombok.RequiredArgsConstructor;
import org.quickstarts.kitchensink.model.User;
import org.quickstarts.kitchensink.pojo.UserSignUpDTO;
import org.quickstarts.kitchensink.repository.UserRepository;
import org.quickstarts.kitchensink.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<String> register(@RequestBody UserSignUpDTO userSignUpDTO) {
        User userByEmail = userRepository.findByEmail(userSignUpDTO.getEmail());
        if (userByEmail != null) {
            return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
        }

        User newUser = new User();
        newUser.setEmail(userSignUpDTO.getEmail());
        newUser.setPassword(userSignUpDTO.getPassword());

        userService.createUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body("Created");
    }

//    @RequestMapping(value = "/profile", method = RequestMethod.GET)
//    public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
//        String userName = request.getUserPrincipal().getName();
//        userService.getUserProfileByUserName(userName);
//        return ResponseEntity.ok(userName);
//    }
}
