package com.webbeat.webbeat.repository;

import com.webbeat.webbeat.model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface PasswordResetRepository extends MongoRepository<PasswordResetToken, String> {

    PasswordResetToken findByToken(String token);

    PasswordResetToken findByUserId(String userId);
}
