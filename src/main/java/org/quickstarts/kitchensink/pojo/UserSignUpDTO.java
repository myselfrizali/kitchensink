package org.quickstarts.kitchensink.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignUpDTO {
    private String email;
    private String password;
}
