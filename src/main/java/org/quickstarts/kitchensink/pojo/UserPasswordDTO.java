package org.quickstarts.kitchensink.pojo;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
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
public class UserPasswordDTO {

    @NotNull(message = "Email is mandatory")
    @Pattern(regexp = EMAIL_REGEX, message = "Invalid email")
    private String email;

    @NotBlank(message = "Existing password is mandatory")
    private String existingPassword;

    @NotNull(message = "Password is mandatory")
    @Pattern(regexp = PASSWORD_REGEX,
            message = "Password must be at least 8 characters long and include uppercase, lowercase, and special characters.")
    private String password;

    @NotBlank(message = "Confirm Password must not be empty")
    private String confirmPassword;

    @AssertTrue(message = "Password and Confirm Password must match")
    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }
}
