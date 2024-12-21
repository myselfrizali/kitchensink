package org.quickstarts.kitchensink.service;

import org.quickstarts.kitchensink.model.Member;
import org.quickstarts.kitchensink.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class MemberRegistrationService {
    private final Logger log = Logger.getLogger(this.getClass().getName());

    private final MemberRepository memberRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public MemberRegistrationService(MemberRepository memberRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.memberRepository = memberRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void register(Member member) {
        log.info("Registering member: " + member.getEmail());
        memberRepository.save(member); // Save the member to MongoDB
        applicationEventPublisher.publishEvent(member); // Publish the event
    }
}
