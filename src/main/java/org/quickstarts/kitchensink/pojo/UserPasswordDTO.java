package org.quickstarts.kitchensink.pojo;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.quickstarts.kitchensink.util.ApplicationConstants;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordDTO {

    @NotNull(message = "Email is mandatory")
    @Pattern(regexp = "^([a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$", message = "Invalid email")
    private String email;

    @NotBlank(message = "Existing password is mandatory")
    private String existingPassword;

    @NotNull(message = "Password is mandatory")
    @Pattern(regexp = ApplicationConstants.PASSWORD_REGEX,
            message = "Password must be at least 8 characters long and include uppercase, lowercase, and special characters.")
    private String password;

    @NotBlank(message = "Confirm Password must not be empty")
    private String confirmPassword;

    @AssertTrue(message = "Password and Confirm Password must match")
    public boolean isPasswordMatch() {
        return password != null && password.equals(confirmPassword);
    }
}
