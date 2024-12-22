package org.quickstarts.kitchensink.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class MemberTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidMember() {
        // Arrange
        Member member = new Member("John Doe", "john.doe@example.com", "1234567890");

        // Act
        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void testMemberNameValidation() {
        // Arrange
        Member member = new Member("John123", "john.doe@example.com", "1234567890");

        // Act
        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Must not contain numbers");
    }

    @Test
    void testMemberEmailValidation() {
        // Arrange
        Member member = new Member("John Doe", "invalid-email", "1234567890");

        // Act
        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("must be a well-formed email address");
    }

    @Test
    void testMemberPhoneNumberValidation() {
        // Arrange
        Member member = new Member("John Doe", "john.doe@example.com", "12345");

        // Act
        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("size must be between 10 and 12");
    }

    @Test
    void testMemberPhoneNumberDigitsValidation() {
        // Arrange
        Member member = new Member("John Doe", "john.doe@example.com", "12345abcdd");

        // Act
        Set<ConstraintViolation<Member>> violations = validator.validate(member);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("numeric value out of bounds (<12 digits>.<0 digits> expected)");
    }

    @Test
    void testMemberConstructorInitialization() {
        // Arrange
        String name = "Jane Doe";
        String email = "jane.doe@example.com";
        String phoneNumber = "0987654321";

        // Act
        Member member = new Member(name, email, phoneNumber);

        // Assert
        assertThat(member.getName()).isEqualTo(name);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber);
    }
}