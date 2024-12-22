package org.quickstarts.kitchensink.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Getter
@Setter
@Document("user")
public class User {
    @Id
    @Indexed(unique = true)
    private String id;

    @NotNull
    @NotEmpty
    @Email
    @Field("email")
    private String email;
    private String password;

    private boolean enabled;
    private Date createdAt;
    private Date updatedAt;

    public User() {
        this.enabled = true;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public User(String email, String password) {
        this();
        this.email = email;
        this.password = password;
    }
}
