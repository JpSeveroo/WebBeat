package com.webbeat.webbeat.model;


import org.hibernate.validator.constraints.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "password_reset_tokens")
public record PasswordResetToken(

        @Id
        String id,
        String token,
        String userId,
        @Indexed(expireAfter = "0s")
        Instant expireDate

) {}
