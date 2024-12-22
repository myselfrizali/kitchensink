package org.quickstarts.kitchensink.service;

import jakarta.validation.ValidationException;
import org.quickstarts.kitchensink.model.Member;
import org.quickstarts.kitchensink.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class MemberService {
    private final Logger log = Logger.getLogger(this.getClass().getName());

    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> findAll() {
        log.info("Finding all members");
        return memberRepository.findAll();
    }

    public Optional<Member> findByEmail(String email) {
        log.info("Finding member by email: " + email);
        return memberRepository.findByEmail(email);
    }

    public List<Member> findAllOrderedByName() {
        log.info("Finding all members by name");
        return memberRepository.findAllOrderedBy(Sort.by("name"));
    }

    public Optional<Member> findById(String id) {
        log.info("Finding a member by id");
        return memberRepository.findById(id);
    }

    public void save(Member member) {
        log.info("Saving a member");
        memberRepository.save(member);
    }

    public boolean isEmailExist(String email) {
        log.info("Checking if email exists");
        boolean isEmailExist = memberRepository.existsByEmail(email);
        if (isEmailExist) {
            throw new ValidationException("Unique Email Violation");
        }

        return false;
    }

    public void delete(Member member) {
        log.info("Deleting a member: " + member.getId());
        member.setDeleted(true);
        memberRepository.save(member);
    }
}
