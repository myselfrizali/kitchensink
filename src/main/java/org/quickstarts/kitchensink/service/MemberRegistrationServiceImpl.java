package org.quickstarts.kitchensink.service;

import lombok.extern.slf4j.Slf4j;
import org.quickstarts.kitchensink.model.Member;
import org.quickstarts.kitchensink.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MemberRegistrationServiceImpl implements MemberRegistrationService {
    private final MemberRepository memberRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public MemberRegistrationServiceImpl(MemberRepository memberRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.memberRepository = memberRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void register(Member member) {
        log.info("Registering member: {}", member.getEmail());
        memberRepository.save(member); // Save the member to MongoDB
        applicationEventPublisher.publishEvent(member); // Publish the event
    }
}
