package org.quickstarts.kitchensink.service;

import lombok.RequiredArgsConstructor;
import org.quickstarts.kitchensink.model.User;
import org.quickstarts.kitchensink.pojo.UserPrincipalDTO;
import org.quickstarts.kitchensink.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        return new UserPrincipalDTO(user);
    }

    @Override
    public void createUser(User user) {
        userRepo.save(user);
    }
}
