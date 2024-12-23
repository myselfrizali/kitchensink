package org.quickstarts.kitchensink.service;

import lombok.extern.slf4j.Slf4j;
import org.quickstarts.kitchensink.model.Member;
import org.quickstarts.kitchensink.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public List<Member> findAll() {
        log.info("Finding all members");
        return memberRepository.findAll();
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        log.info("Finding member by email: {}", email);
        return memberRepository.findByEmail(email);
    }

    @Override
    public List<Member> findAllOrderedByName() {
        log.info("Finding all members by name");
        return memberRepository.findAllOrderedBy(Sort.by("name"));
    }

    @Override
    public Optional<Member> findById(String id) {
        log.info("Finding a member by id");
        return memberRepository.findById(id);
    }

    @Override
    public void save(Member member) {
        log.info("Saving a member");
        memberRepository.save(member);
    }

    @Override
    public boolean isEmailExist(String email) {
        log.info("Checking if email exists");
        return memberRepository.existsByEmail(email);
    }

    @Override
    public void delete(Member member) {
        log.info("Deleting a member: {}", member.getId());
        member.setDeleted(true);
        memberRepository.save(member);
    }
}
