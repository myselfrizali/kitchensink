package org.quickstarts.kitchensink.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@Document("user")
public class User {
    @Id
    @Indexed(unique = true)
    private String id;

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
}
