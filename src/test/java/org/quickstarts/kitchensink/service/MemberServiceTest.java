package org.quickstarts.kitchensink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quickstarts.kitchensink.model.Member;
import org.quickstarts.kitchensink.repository.MemberRepository;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        // Arrange
        Member member1 = new Member("Alice", "alice@example.com", "1234567890");
        Member member2 = new Member("bob", "bob@example.com", "9876543210");
        List<Member> members = Arrays.asList(member1, member2);

        when(memberRepository.findAll()).thenReturn(members);

        // Act
        List<Member> result = memberService.findAll();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(member1, member2);
        verify(memberRepository, times(1)).findAll();
    }

    @Test
    void testFindAllOrderedByName() {
        // Arrange
        Member member1 = new Member("Alice", "alice@example.com", "1234567890");
        Member member2 = new Member("Bob", "bob@example.com", "9876543210");
        List<Member> members = Arrays.asList(member1, member2);

        when(memberRepository.findAllOrderedBy(Sort.by("name"))).thenReturn(members);

        // Act
        List<Member> result = memberService.findAllOrderedByName();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("Alice");
        assertThat(result.get(1).getName()).isEqualTo("Bob");
        verify(memberRepository, times(1)).findAllOrderedBy(Sort.by("name"));
    }

    @Test
    void testFindById_found() {
        // Arrange
        String memberId = "1";
        Member member = new Member("Alice", "alice@example.com", "1234567890");
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // Act
        Optional<Member> result = memberService.findById(memberId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(member);
        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    void testFindById_notFound() {
        // Arrange
        String memberId = "1";
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // Act
        Optional<Member> result = memberService.findById(memberId);

        // Assert
        assertThat(result).isEmpty();
        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    void testSave() {
        // Arrange
        Member member = new Member("Alice", "alice@example.com", "1234567890");

        // Act
        memberService.save(member);

        // Assert
        verify(memberRepository, times(1)).save(member);
    }

    @Test
    void testIsEmailExist_emailExists() {
        // Arrange
        String email = "alice@example.com";
        when(memberRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        boolean emailExist = memberService.isEmailExist(email);
        assertThat(emailExist).isTrue();
        verify(memberRepository, times(1)).existsByEmail(email);
    }

    @Test
    void testIsEmailExist_emailDoesNotExist() {
        // Arrange
        String email = "alice@example.com";
        when(memberRepository.existsByEmail(email)).thenReturn(false);

        // Act
        boolean result = memberService.isEmailExist(email);

        // Assert
        assertThat(result).isFalse();
        verify(memberRepository, times(1)).existsByEmail(email);
    }
}
