package com.webbeat.webbeat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public record User( //Só pra explicar, tamo usando record por tratarmos de dados imutáveis
        @Id
        String id,
        String email,
        String passwordHash
) {}
