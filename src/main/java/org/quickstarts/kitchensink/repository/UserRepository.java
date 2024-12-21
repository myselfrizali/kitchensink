package org.quickstarts.kitchensink.repository;

import org.quickstarts.kitchensink.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, Integer> {
    User findByEmail(String email);
}
