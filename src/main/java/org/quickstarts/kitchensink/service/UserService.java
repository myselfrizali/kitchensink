package org.quickstarts.kitchensink.service;

import org.quickstarts.kitchensink.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    void saveOrUpdate(User user);
}
