package org.quickstarts.kitchensink.pojo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static org.quickstarts.kitchensink.util.ApplicationConstants.EMAIL_REGEX;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDTO {

    @NotNull(message = "Username is mandatory")
    @Pattern(regexp = EMAIL_REGEX, message = "Invalid username")
    private String username;

    @NotBlank(message = "Password is mandatory")
    private String password;
}
