package com.chat_app.repository;

import com.chat_app.constant.RoleName;
import com.chat_app.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {
    Role findByName(RoleName name);
}
