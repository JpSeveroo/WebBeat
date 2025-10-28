package com.webbeat.webbeat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chats")
public record Chats(
        @Id
        String id,
        String telegramId,
        String ownerId,
        String name
) {}
