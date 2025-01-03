package org.quickstarts.kitchensink.pojo;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberDTOTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidMember() {
        // Arrange
        MemberDTO member = new MemberDTO("John Doe", "john.doe@example.com", "1234567890");

        // Act
        Set<ConstraintViolation<MemberDTO>> violations = validator.validate(member);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void testMemberNameValidation() {
        // Arrange
        MemberDTO member = new MemberDTO("John123", "john.doe@example.com", "1234567890");

        // Act
        Set<ConstraintViolation<MemberDTO>> violations = validator.validate(member);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Must not contain numbers");
    }

    @Test
    void testMemberEmailValidation() {
        // Arrange
        MemberDTO member = new MemberDTO("John Doe", "invalid-email", "1234567890");

        // Act
        Set<ConstraintViolation<MemberDTO>> violations = validator.validate(member);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Invalid email");
    }

    @Test
    void testMemberPhoneNumberValidation() {
        // Arrange
        MemberDTO member = new MemberDTO("John Doe", "john.doe@example.com", "12345");

        // Act
        Set<ConstraintViolation<MemberDTO>> violations = validator.validate(member);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Phone number must be 10 to 12 digits.");
    }

    @Test
    void testMemberPhoneNumberDigitsValidation() {
        // Arrange
        MemberDTO member = new MemberDTO("John Doe", "john.doe@example.com", "12345abcdd");

        // Act
        Set<ConstraintViolation<MemberDTO>> violations = validator.validate(member);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Must be numbers only");
    }

    @Test
    void testMemberConstructorInitialization() {
        // Arrange
        String name = "Jane Doe";
        String email = "jane.doe@example.com";
        String phoneNumber = "0987654321";

        // Act
        MemberDTO member = new MemberDTO(name, email, phoneNumber);

        // Assert
        assertThat(member.getName()).isEqualTo(name);
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getPhoneNumber()).isEqualTo(phoneNumber);
    }
}
