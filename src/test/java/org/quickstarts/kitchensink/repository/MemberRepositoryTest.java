package org.quickstarts.kitchensink.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quickstarts.kitchensink.model.Member;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberRepositoryTest {

    @Mock
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testMember = new Member("Test User", "test@example.com", "1234567890");
    }

    @Test
    void testFindByEmail_memberFound() {
        // Arrange
        String email = "test@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(testMember));

        // Act
        Optional<Member> result = memberRepository.findByEmail(email);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        assertThat(result.get().getName()).isEqualTo(testMember.getName());
        verify(memberRepository, times(1)).findByEmail(email);
    }

    @Test
    void testFindByEmail_memberNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<Member> result = memberRepository.findByEmail(email);

        // Assert
        assertThat(result).isNotPresent();
        verify(memberRepository, times(1)).findByEmail(email);
    }

    @Test
    void testFindAllOrderedBy() {
        // Arrange
        List<Member> members = List.of(
                new Member("User One", "user1@example.com", "1234567890"),
                new Member("User Two", "user2@example.com", "9876543210")
        );
        Sort sort = Sort.by(Sort.Order.asc("name"));
        when(memberRepository.findAllOrderedBy(sort)).thenReturn(members);

        // Act
        List<Member> result = memberRepository.findAllOrderedBy(sort);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("User One");
        assertThat(result.get(1).getName()).isEqualTo("User Two");
        verify(memberRepository, times(1)).findAllOrderedBy(sort);
    }

    @Test
    void testExistsByEmail_memberExists() {
        // Arrange
        String email = "test@example.com";
        when(memberRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean result = memberRepository.existsByEmail(email);

        // Assert
        assertThat(result).isTrue();
        verify(memberRepository, times(1)).existsByEmail(email);
    }

    @Test
    void testExistsByEmail_memberDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        when(memberRepository.existsByEmail(email)).thenReturn(false);

        // Act
        boolean result = memberRepository.existsByEmail(email);

        // Assert
        assertThat(result).isFalse();
        verify(memberRepository, times(1)).existsByEmail(email);
    }
}
