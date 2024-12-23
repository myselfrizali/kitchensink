package org.quickstarts.kitchensink.service;

import org.quickstarts.kitchensink.model.Member;

import java.util.List;
import java.util.Optional;

public interface MemberService {

    List<Member> findAll();

    Optional<Member> findByEmail(String email);

    List<Member> findAllOrderedByName();

    Optional<Member> findById(String id);

    void save(Member member);

    boolean isEmailExist(String email);

    void delete(Member member);
}
