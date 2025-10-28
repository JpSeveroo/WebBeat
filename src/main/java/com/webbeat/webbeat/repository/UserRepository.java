package com.webbeat.webbeat.repository;

import com.webbeat.webbeat.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
