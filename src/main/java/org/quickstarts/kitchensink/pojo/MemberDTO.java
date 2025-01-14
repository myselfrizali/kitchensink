package org.quickstarts.kitchensink.pojo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static org.quickstarts.kitchensink.util.ApplicationConstants.EMAIL_REGEX;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    @NotBlank(message = "Name is mandatory")
    @Size(min = 1, max = 25, message = "Name must of length {min} to {max}")
    @Pattern(regexp = "[^0-9]*", message = "Must not contain numbers")
    private String name;

    @NotBlank(message = "Email is mandatory")
    @Pattern(regexp = EMAIL_REGEX, message = "Invalid email")
    private String email;

    @NotNull(message = "Phone number is mandatory")
    @Size(min = 10, max = 12, message = "Phone number must be {min} to {max} digits.")
    @Pattern(regexp = "^[0-9]*", message = "Must be numbers only")
    private String phoneNumber;
}
