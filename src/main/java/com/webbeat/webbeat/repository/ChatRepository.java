package com.webbeat.webbeat.repository;

import com.webbeat.webbeat.model.Chats;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRepository extends MongoRepository<Chats, String> {
}
