package org.quickstarts.kitchensink.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Document("member")
public class Member implements Serializable {
    @Id
    @Indexed(unique = true)
    private String id;

    @Field("name")
    private String name;

    @Field("email")
    private String email;

    @Field("phone_number")
    private String phoneNumber;

    @JsonIgnore
    @Field("is_deleted")
    private boolean isDeleted;

    @Field("is_active")
    private boolean isActive;

    public Member(String name, String email, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.isDeleted = false;
        this.isActive = true;
    }
}
