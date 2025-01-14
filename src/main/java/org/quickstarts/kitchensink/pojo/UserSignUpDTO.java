package org.quickstarts.kitchensink.pojo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpDTO {

    @NotNull(message = "Email is mandatory")
    @Pattern(regexp = "^([a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$", message = "Invalid email")
    private String email;

    @NotNull(message = "Password is mandatory")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.])[A-Za-z\\d!@#$%^&*(),.]{8,}$",
            message = "Password must be at least 8 characters long and include uppercase, lowercase, and special characters.")
    private String password;
}
