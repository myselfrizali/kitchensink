package org.quickstarts.kitchensink.pojo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static org.quickstarts.kitchensink.util.ApplicationConstants.EMAIL_REGEX;
import static org.quickstarts.kitchensink.util.ApplicationConstants.PASSWORD_REGEX;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpDTO {

    @NotNull(message = "Email is mandatory")
    @Pattern(regexp = EMAIL_REGEX, message = "Invalid email")
    private String email;

    @NotNull(message = "Password is mandatory")
    @Pattern(regexp = PASSWORD_REGEX,
            message = "Password must be at least 8 characters long and include uppercase, lowercase, and special characters.")
    private String password;
}
