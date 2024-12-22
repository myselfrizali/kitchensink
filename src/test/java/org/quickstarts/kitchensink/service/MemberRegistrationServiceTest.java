package org.quickstarts.kitchensink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quickstarts.kitchensink.model.Member;
import org.quickstarts.kitchensink.repository.MemberRepository;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MemberRegistrationServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private MemberRegistrationService memberRegistrationService;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_memberSuccessfullyRegistered() {
        // Arrange
        Member member = new Member("Test", "test@example.com", "1234567890");

        // Act
        memberRegistrationService.register(member);

        // Assert: Verify that save() was called on the memberRepository
        verify(memberRepository, times(1)).save(member);

        // Assert: Verify that publishEvent() was called on the applicationEventPublisher
        verify(applicationEventPublisher, times(1)).publishEvent(member);
    }

}
