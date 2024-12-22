package org.quickstarts.kitchensink.repository;

import org.quickstarts.kitchensink.model.Member;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByIdAndIsActive(String id);
    List<Member> findAllOrderedBy(Sort sort);

    boolean existsByEmail(String email);
}
